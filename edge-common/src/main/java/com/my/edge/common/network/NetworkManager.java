package com.my.edge.common.network;

import com.my.edge.common.control.ControlSignal;
import com.my.edge.common.control.response.Response;
import com.my.edge.common.control.command.Command;
import com.my.edge.common.data.DataWrapper;
import com.my.edge.common.entity.Tuple2;
import com.my.edge.common.util.JsonSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NetworkManager implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(NetworkManager.class);

    public static final int SERVER_AND_CLIENT = 0;
    public static final int SERVER_ONLY = 1;
    public static final int CLIENT_ONLY = 2;

    private ConcurrentMap<SocketAddress, SocketChannel> connections;
    private int serverPort;
    private Bootstrap client;
    private EventLoopGroup clientGroup;
    private ServerBootstrap server;
    private EventLoopGroup serverBossGroup;
    private EventLoopGroup serverWorkerGroup;
    private MessagePool messagePool;
    private int mode;

    public NetworkManager(int port) {
        this.connections = new ConcurrentHashMap<>();
        this.serverPort = port;
        this.messagePool = new MessagePool();
        this.mode = SERVER_AND_CLIENT;
    }

    public NetworkManager(int port, int mode) {
        this(port);
        this.mode = mode;
    }

    public void initialize() {
        switch (mode) {
            case SERVER_AND_CLIENT:
                initializeClient();
                initializeServer();
                break;
            case SERVER_ONLY:
                initializeServer();
                break;
            case CLIENT_ONLY:
                initializeClient();
                break;
            default:
                throw new RuntimeException("Unrecognized NetworkManager mode " + mode);
        }
    }

    private void initializeClient() {
        clientGroup = new NioEventLoopGroup();
        try {
            client = new Bootstrap();
            client.group(clientGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                            .addLast(new DataPacketDecoder())
                            .addLast(new DataPacketEncoder())
                            .addLast(new DataHandler(messagePool));
                }
            });
        } catch (Exception e) {
            if (clientGroup != null) {
                clientGroup.shutdownGracefully();
            }
            throw new RuntimeException("Initializing network client failed. ", e);
        }
    }

    private void initializeServer() {
        serverBossGroup = new NioEventLoopGroup();
        serverWorkerGroup = new NioEventLoopGroup();
        try {
            server = new ServerBootstrap();
            server.group(serverBossGroup, serverWorkerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            InetSocketAddress remoteAddress = ch.remoteAddress();
                            if (connections.containsKey(remoteAddress)) {
                                // 不应该走到这里
                                throw new RuntimeException("Duplicate channel detects for remote address " + remoteAddress);
                            }
                            logger.info("Receiving connection from remote node " + remoteAddress);
                            connections.put(remoteAddress, ch);
                            ch.pipeline().addLast(new DataPacketDecoder())
                                    .addLast(new DataPacketEncoder())
                                    .addLast(new DataHandler(messagePool));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
        } catch(Exception e) {
            if (serverBossGroup != null) {
                serverBossGroup.shutdownGracefully();
            }
            if (serverWorkerGroup != null) {
                serverWorkerGroup.shutdownGracefully();
            }
            throw new RuntimeException("Initializing network server failed. ", e);
        }
    }

    public void shutdownClient() {
        if (clientGroup != null) {
            clientGroup.shutdownGracefully();
        }
    }

    @Override
    public void run() {
        if (mode == SERVER_AND_CLIENT || mode == SERVER_ONLY) {
            try {
                ChannelFuture future = server.bind(serverPort);
                future.sync();
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                throw new RuntimeException("Error occurs in NetworkManager. ", e);
            }
        }
    }

    private void send(SocketAddress target, Object object) {
        SocketChannel channel = connections.get(target);
        if (channel == null) {
            channel = establishNewChannel(target);
        }
        channel.writeAndFlush(object);
    }

    public void sendResponseAndClose(SocketAddress target, Response response) {
        SocketChannel channel = connections.remove(target);
        if (channel == null) {
            channel = establishNewChannel(target);
        }
        channel.writeAndFlush(response);
        channel.close();
    }

    public void sendData(SocketAddress target, DataWrapper dataWrapper) {
        try {
            logger.debug("Sending data " + JsonSerializer.objectMapper.writeValueAsString(dataWrapper) + " to " + target);
        } catch (Exception e) {
            logger.warn("", e);
        }
        send(target, dataWrapper);
    }

    public void sendCommand(SocketAddress target, Command command) {
        try {
            logger.debug("Sending command " + JsonSerializer.objectMapper.writeValueAsString(command) + " to " + target);
        } catch (Exception e) {
            logger.warn("", e);
        }
        send(target, command);
    }

    public void sendResponse(SocketAddress target, Response response) {
        try {
            logger.debug("Sending command " + JsonSerializer.objectMapper.writeValueAsString(response) + " to " + target);
        } catch (Exception e) {
            logger.warn("", e);
        }
        send(target, response);
    }

    public Tuple2<SocketAddress, ControlSignal> fetchControlSignal() {
        return messagePool.fetchControlSignal();
    }

    public Tuple2<SocketAddress, DataWrapper> fetchDataWrapper() {
        return messagePool.fetchDataWrapper();
    }

    public void addDataWrapper(SocketAddress invoker, DataWrapper dataWrapper) {
        messagePool.addDataWrapper(invoker, dataWrapper);
    }

    private SocketChannel establishNewChannel(SocketAddress target) {
        try {
            SocketChannel socketChannel = (SocketChannel) client.connect(target).sync().channel();
            logger.info("Establishing new connection for remote NodeManager " + target);
            connections.put(target, socketChannel);
            return socketChannel;
        } catch (Exception e) {
            throw new RuntimeException("Creating new channel to " + target + " failed. ", e);
        }
    }

    private SocketChannel establishNewChannel(String host, int port) {
        SocketAddress remoteAddress = new InetSocketAddress(host, port);
        return establishNewChannel(remoteAddress);
    }

    public void releaseConnection(SocketAddress socketAddress) {
        SocketChannel channel = connections.remove(socketAddress);
        try {
            if (channel != null) {
                channel.close().sync();
            }
        } catch (Exception e) {
            logger.warn("Releasing connection for remote node " + socketAddress + " failed. ", e);
        }
    }
}
