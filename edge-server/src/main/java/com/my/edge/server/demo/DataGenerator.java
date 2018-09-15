package com.my.edge.server.demo;

import com.my.edge.common.data.DataTag;
import com.my.edge.server.ServerHandler;
import com.my.edge.server.util.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataGenerator implements Runnable {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ServerHandler serverHandler;
    private DataTag dataTag;
    private long id;
    private int mark;

    public DataGenerator(ServerHandler serverHandler, int mark) {
        this.serverHandler = serverHandler;
        this.dataTag = DemoDataTag.DEMO_DATA_TAG_1;
        this.id = 0L;
        this.mark = mark;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100000L);
                SimpleData data = new SimpleData();
                data.setDataTag(dataTag);
                data.setValue("Message for test-data from node " + mark + " - index(" + id + "). ");
                id++;
                logger.debug("Generated data " + JsonSerializer.objectMapper.writeValueAsString(data));
                serverHandler.addData(null, data);
            } catch (Exception e) {
                logger.warn("Generating data encounters problem. ", e);
            }
        }
    }
}
