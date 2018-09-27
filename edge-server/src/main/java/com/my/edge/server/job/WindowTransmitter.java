package com.my.edge.server.job;

import com.my.edge.common.control.NodeFilter;
import com.my.edge.common.data.Data;
import com.my.edge.common.data.DataTag;
import com.my.edge.common.entity.Tuple2;
import com.my.edge.server.ServerHandler;
import com.my.edge.server.util.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Creator: Beefman
 * Date: 2018/9/21
 */
public class WindowTransmitter implements Transmitter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ServerHandler serverHandler;
    private DataTag dataTag;
    private NodeFilter nodeFilter;
    private long timeMark;
    private long interval = 30000L;
    private Map<Long, Queue<Data>> dataBuffer;
    private Map<Long, QueueIterator> dataIterators;
    private Queue<Long> timeMarkBuffer;
    private Thread monitorThread;


    public WindowTransmitter(ServerHandler serverHandler, DataTag dataTag, NodeFilter nodeFilter) {
        this.serverHandler = serverHandler;
        this.dataTag = dataTag;
        this.nodeFilter = nodeFilter;
        this.dataBuffer = new HashMap<>();
        this.dataIterators = new HashMap<>();
        this.timeMarkBuffer = new PriorityQueue<>();
        this.timeMark = 0L;
        this.monitorThread = new Thread(new Monitor());
    }

    public void prepareData() {
        logger.info("Requesting data with data tag " + JsonSerializer.writeValueAsString(dataTag) + " and node filter " + JsonSerializer.writeValueAsString(nodeFilter));
        serverHandler.requestData(nodeFilter, dataTag, this);
        timeMark = System.currentTimeMillis();
        monitorThread.start();
    }

    public synchronized void addNewData(Data data) {
        long timestamp = data.getTimestamp();
        if (timestamp >= timeMark) {
            int offset = (int)((timestamp - timeMark) / interval);
            long thisTimeMark = timeMark + offset * interval;
            Queue<Data> buffer = dataBuffer.computeIfAbsent(thisTimeMark, (key) -> {
                logger.debug("Create a new queue to hold data whose time slot is [" + key + ", " + (key + interval) + "). ");
                timeMarkBuffer.add(key);
                return new LinkedList<>();
            });
            if (!dataIterators.containsKey(thisTimeMark)) {
                dataIterators.put(thisTimeMark, new QueueIterator(buffer, thisTimeMark));
            }
            buffer.add(data);
        } else {
            logger.debug("Timestamp of data is " + timestamp + ", which is later than time mark " + timeMark + ". Ignoring this data. ");
        }
    }

    public synchronized Tuple2<DataTag, Iterator<Data>> fetchData() {
        Long pendingTimeMark = timeMarkBuffer.poll();
        if (pendingTimeMark == null) {
            return null;
        } else {
            QueueIterator iterator = dataIterators.get(pendingTimeMark);
            logger.debug("Get data in time slot [" + pendingTimeMark + ", " + (pendingTimeMark + interval) + ". ");
            return new Tuple2<>(dataTag, iterator);
        }
    }

    private class QueueIterator implements Iterator<Data> {
        private boolean stop;
        private final Queue<Data> queue;
        private Data nextValue;
        private long startTime;

        public QueueIterator(Queue<Data> queue, long startTime) {
            this.stop = false;
            this.queue = queue;
            this.startTime = startTime;
        }

        @Override
        public boolean hasNext() {
            while (true) {
                synchronized (queue) {
                    nextValue = queue.poll();
                }
                if (nextValue != null) {
                    return true;
                } else if (stop) {
                    logger.debug("Stop queue iterator whose time slot is [" + startTime + ", " + (startTime + interval) + "). ");
                    return false;
                }
            }
        }

        @Override
        public Data next() {
            return nextValue;
        }

        synchronized void stop() {
            stop = true;
        }
    }

    private class Monitor implements Runnable{

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000L);
                    long timestamp = System.currentTimeMillis();
                    if (timestamp >= timeMark + interval) {
                        long oldTimeMark = timeMark;
                        timeMark += interval;
                        for (Map.Entry<Long, Queue<Data>> entry: dataBuffer.entrySet()) {
                            Long time = entry.getKey();
                            if (time < timeMark) {
                                dataBuffer.remove(time);
                                QueueIterator queueIterator = dataIterators.get(time);
                                queueIterator.stop();
                                logger.debug("Stop receiving data whose timestamp is in slot [" + oldTimeMark + ", " + timeMark + "). ");
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Monitor occurs exception. ", e);
                }
            }
        }
    }
}
