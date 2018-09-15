package com.my.edge.common.network;

import com.my.edge.common.control.ControlSignal;
import com.my.edge.common.data.DataWrapper;
import com.my.edge.common.util.JsonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class DataPacketEncoder extends MessageToByteEncoder<Object> {

    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        Datagram datagram = new Datagram();
        if (msg instanceof DataWrapper) {
            datagram.setDatagramType(DatagramType.DATA);
        } else if (msg instanceof ControlSignal) {
            datagram.setDatagramType(DatagramType.CONTROL_SIGNAL);
        } else {
            throw new RuntimeException("Should never reach here. ");
        }
        byte[] data = JsonSerializer.objectMapper.writeValueAsBytes(msg);
        datagram.setContent(data);
        datagram.setLength(data.length);
        out.writeBytes(datagram.serialize());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.fireExceptionCaught(cause);
    }
}
