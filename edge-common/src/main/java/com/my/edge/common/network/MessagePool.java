package com.my.edge.common.network;

import com.my.edge.common.control.ControlSignal;
import com.my.edge.common.data.DataWrapper;
import com.my.edge.common.entity.Tuple2;

import java.net.SocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Creator: Beefman
 * Date: 2018/9/14
 */
public class MessagePool {
    private BlockingQueue<Tuple2<SocketAddress, ControlSignal>> controlSignals = new ArrayBlockingQueue<>(1000);
    private BlockingQueue<Tuple2<SocketAddress, DataWrapper>> datum = new ArrayBlockingQueue<>(1000);

    public void addControlSignal(SocketAddress requester, ControlSignal controlSignal) {
        controlSignals.add(new Tuple2<>(requester, controlSignal));
    }

    public void addDataWrapper(SocketAddress invoker, DataWrapper dataWrapper) {
        datum.add(new Tuple2<>(invoker, dataWrapper));
    }

    public Tuple2<SocketAddress, ControlSignal> fetchControlSignal() {
        try {
            return controlSignals.take();
        } catch (Exception e) {
            throw new RuntimeException("Fetching control signal failed. ");
        }
    }

    public Tuple2<SocketAddress, DataWrapper> fetchDataWrapper() {
        try {
            return datum.take();
        } catch (Exception e) {
            throw new RuntimeException("Fetching data wrapper failed. ");
        }
    }
}
