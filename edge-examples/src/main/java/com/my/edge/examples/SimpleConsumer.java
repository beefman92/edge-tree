package com.my.edge.examples;

import com.my.edge.common.job.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creator: Beefman
 * Date: 2018/9/14
 */
public class SimpleConsumer implements Consumer<SimpleDataTag, SimpleData> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void consume(SimpleDataTag dataTag, Iterable<SimpleData> values) {
        logger.info("Hello World!");
    }
}
