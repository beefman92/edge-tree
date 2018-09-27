package com.my.edge.server.job;

import com.my.edge.common.data.Data;

/**
 * Creator: Beefman
 * Date: 2018/9/27
 */
public interface Transmitter {
    void prepareData();
    void addNewData(Data data);
}
