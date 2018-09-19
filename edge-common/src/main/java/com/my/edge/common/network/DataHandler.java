package com.my.edge.common.network;

import com.my.edge.common.control.ControlSignal;
import com.my.edge.common.data.DataWrapper;
import com.my.edge.common.util.JsonSerializer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class DataHandler extends ChannelInboundHandlerAdapter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private MessagePool messagePool;

    public DataHandler(MessagePool messagePool) {
        this.messagePool = messagePool;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketAddress socketAddress = ctx.pipeline().channel().remoteAddress();
        if (msg instanceof ControlSignal) {
            logger.debug("Received control signal " + JsonSerializer.objectMapper.writeValueAsString(msg) +
                    " from " + socketAddress);
            ControlSignal controlSignal = (ControlSignal) msg;
            messagePool.addControlSignal(socketAddress, controlSignal);
        } else if (msg instanceof DataWrapper) {
            logger.debug("Received data " + JsonSerializer.objectMapper.writeValueAsString(msg) +
                    " from " + socketAddress);
            DataWrapper dataWrapper = (DataWrapper) msg;
            messagePool.addDataWrapper(socketAddress, dataWrapper);
        } else {
            throw new RuntimeException("Should never reach here. ");
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("DataHandler encounters exception. ", cause);
        ctx.fireExceptionCaught(cause);
    }
}
