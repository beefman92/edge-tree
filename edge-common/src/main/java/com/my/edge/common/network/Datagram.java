package com.my.edge.common.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * Creator: Beefman
 * Date: 2018/7/21
 */
public class Datagram {
    public static final int TAG = 0xDEADBEEF;
    DatagramType datagramType;
    int length;
    byte[] content;

    public DatagramType getDatagramType() {
        return datagramType;
    }

    public void setDatagramType(DatagramType datagramType) {
        this.datagramType = datagramType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public byte[] serialize() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4 + 4 + 4 + length + 4);
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeInt(TAG);
            dataOutputStream.writeInt(datagramType.getValue());
            dataOutputStream.writeInt(this.length);
            dataOutputStream.write(this.content);
            dataOutputStream.writeInt(TAG);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Serializing Datagram failed. ", e);
        }
    }
}
