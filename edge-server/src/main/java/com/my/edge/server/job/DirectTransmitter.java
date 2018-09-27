package com.my.edge.server.job;

import com.my.edge.common.control.NodeFilter;
import com.my.edge.common.data.Data;
import com.my.edge.common.data.DataTag;
import com.my.edge.common.entity.Tuple2;
import com.my.edge.server.ServerHandler;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Creator: Beefman
 * Date: 2018/9/27
 */
public class DirectTransmitter implements Transmitter {
    private ServerHandler serverHandler;
    private DataTag dataTag;
    private NodeFilter nodeFilter;
    private Queue<Data> dataBuffer;

    public DirectTransmitter(ServerHandler serverHandler, DataTag dataTag, NodeFilter nodeFilter) {
        this.serverHandler = serverHandler;
        this.dataTag = dataTag;
        this.nodeFilter = nodeFilter;
        this.dataBuffer = new LinkedList<>();
    }

    @Override
    public void prepareData() {
        serverHandler.requestData(nodeFilter, dataTag, this);
    }

    @Override
    public synchronized void addNewData(Data data) {
        dataBuffer.add(data);
    }

    public synchronized Tuple2<DataTag, Data> fetchData() {
        Data data = dataBuffer.poll();
        if (data == null) {
            return null;
        } else {
            return new Tuple2<>(dataTag, data);
        }
    }
}
