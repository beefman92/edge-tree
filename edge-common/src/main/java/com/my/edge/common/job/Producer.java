package com.my.edge.common.job;

import com.my.edge.common.data.Data;
import com.my.edge.common.data.DataTag;

/**
 * Creator: Beefman
 * Date: 2018/9/14
 */
public interface Producer<DATA_TAG_IN extends DataTag, DATA_IN extends Data, DATA_TAG_OUT extends DataTag, DATA_OUT extends Data> {
    void produce(DATA_TAG_IN dataTag, DATA_IN data_in);
}
