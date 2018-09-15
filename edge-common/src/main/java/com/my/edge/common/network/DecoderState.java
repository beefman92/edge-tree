package com.my.edge.common.network;

/**
 * Creator: Beefman
 * Date: 2018/7/21
 */
public enum  DecoderState {
    READ_HEADER, READ_TYPE, READ_LENGTH, READ_CONTENT, READ_FOOTER;
}
