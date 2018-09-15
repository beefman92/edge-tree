package com.my.edge.common.network;

import com.my.edge.common.control.ControlSignal;
import com.my.edge.common.data.DataWrapper;
import com.my.edge.common.util.JsonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * Creator: Beefman
 * Date: 2018/7/21
 */
public class DataPacketDecoder extends ReplayingDecoder<DecoderState> {
    DatagramType datagramType = null;
    int length = 0;
    byte[] content = null;

    public DataPacketDecoder() {
        super(DecoderState.READ_HEADER);
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        DecoderState state = state();

        switch (state) {
            case READ_HEADER:
                int header = in.readInt();
                if (header == Datagram.TAG) {
                    checkpoint(DecoderState.READ_TYPE);
                } else {
                    throw new RuntimeException("Never reach here. ");
                }
            case READ_TYPE:
                datagramType = DatagramType.getDatagramType(in.readInt());
                checkpoint(DecoderState.READ_LENGTH);
            case READ_LENGTH:
                length = in.readInt();
                content = new byte[length];
                if (length > 0) {
                    checkpoint(DecoderState.READ_CONTENT);
                } else {
                    throw new RuntimeException("Never reach here. ");
                }
            case READ_CONTENT:
                in.readBytes(content);
                checkpoint(DecoderState.READ_FOOTER);
            case READ_FOOTER:
                int footer = in.readInt();
                if (footer == Datagram.TAG) {
                    checkpoint(DecoderState.READ_HEADER);
                    Object object = null;
                    if (datagramType == DatagramType.CONTROL_SIGNAL) {
                        object = JsonSerializer.objectMapper.readValue(content, ControlSignal.class);
                    } else if (datagramType == DatagramType.DATA) {
                        object = JsonSerializer.objectMapper.readValue(content, DataWrapper.class);
                    } else {
                        throw new RuntimeException("Should never reach here. ");
                    }
                    out.add(object);
                } else {
                    throw new RuntimeException("Never reach here. ");
                }
                break;
            default:
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.fireExceptionCaught(cause);
    }
}