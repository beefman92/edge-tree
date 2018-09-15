package com.my.edge.common.network;

/**
 * Creator: Beefman
 * Date: 2018/7/26
 */
public enum DatagramType {
    CONTROL_SIGNAL(0), DATA(1);
    private final int value;

    DatagramType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static DatagramType getDatagramType(int value) {
        switch (value) {
            case 0:
                return CONTROL_SIGNAL;
            case 1:
                return DATA;
            default:
                throw new RuntimeException("Unsupported DatagramType: " + value);
        }
    }
}
