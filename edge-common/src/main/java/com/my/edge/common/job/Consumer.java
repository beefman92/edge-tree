package com.my.edge.common.job;

import com.my.edge.common.data.Data;
import com.my.edge.common.data.DataTag;

/**
 * Creator: Beefman
 * Date: 2018/9/14
 */
public interface Consumer<DATA_TAG_IN extends DataTag, DATA_IN extends Data> {
    void consume(DATA_TAG_IN dataTag, Iterable<DATA_IN> values);
}
