package com.my.edge.examples;

import com.my.edge.common.entity.Tuple2;
import com.my.edge.common.job.Producer;
import com.my.edge.common.util.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creator: Beefman
 * Date: 2018/9/14
 */
public class SimpleProducer implements Producer<DemoDataTag, SimpleData, DemoDataTag, SimpleData> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private int count = 0;

    @Override
    public Tuple2<DemoDataTag, SimpleData> produce(DemoDataTag dataTag, SimpleData simpleData) {
        DemoDataTag demoDataTag = DemoDataTag.DEMO_DATA_TAG_1;
        SimpleData simpleData1 = new SimpleData();
        simpleData1.setDemoDataTag(demoDataTag);
        simpleData1.setField("Content, index(" + count++ + ")");
        simpleData1.setTimestamp(System.currentTimeMillis());
        try {
            Thread.sleep(10000L);
        } catch (Exception e) {
            logger.warn("SimpleProducer encounters error. ", e);
        }
        logger.debug("Generate data " + JsonSerializer.writeValueAsString(simpleData1));
        return new Tuple2<>(demoDataTag, simpleData1);
    }
}
