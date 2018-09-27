package com.my.edge.examples;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.my.edge.common.data.Data;
import com.my.edge.common.data.DataTag;

/**
 * Creator: Beefman
 * Date: 2018/9/14
 */
public class SimpleData implements Data<String> {
    private String field;
    private long timestamp;
    private DemoDataTag demoDataTag = DemoDataTag.DEMO_DATA_TAG_1;

    @JsonIgnore
    @Override
    public DataTag getDataTag() {
        return demoDataTag;
    }

    @JsonIgnore
    @Override
    public String getValue() {
        return null;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public DemoDataTag getDemoDataTag() {
        return demoDataTag;
    }

    public void setDemoDataTag(DemoDataTag demoDataTag) {
        this.demoDataTag = demoDataTag;
    }

    public void setSimpleDataTag(DemoDataTag demoDataTag) {
        this.demoDataTag = demoDataTag;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
