package com.my.edge.common.job;

import com.my.edge.common.data.Data;
import com.my.edge.common.data.DataTag;

import java.util.Iterator;

/**
 * Creator: Beefman
 * Date: 2018/9/14
 */
public interface Consumer<DATA_TAG_IN extends DataTag, DATA_IN extends Data> {
    void consume(DATA_TAG_IN dataTag, Iterator<DATA_IN> values);
}
