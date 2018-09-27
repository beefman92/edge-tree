package com.my.edge.examples;

import com.my.edge.common.data.DataTag;
import com.my.edge.common.job.Consumer;
import com.my.edge.common.util.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Creator: Beefman
 * Date: 2018/9/14
 */
public class SimpleConsumer implements Consumer<DemoDataTag, SimpleData> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void consume(DemoDataTag dataTag, Iterator<SimpleData> values) {
        while (values.hasNext()) {
            SimpleData simpleData = values.next();
            logger.info("Get data " + JsonSerializer.writeValueAsString(simpleData));
        }
    }
}
