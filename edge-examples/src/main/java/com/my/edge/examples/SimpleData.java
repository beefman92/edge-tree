package com.my.edge.examples;

import com.my.edge.common.data.Data;
import com.my.edge.common.data.DataTag;

/**
 * Creator: Beefman
 * Date: 2018/9/14
 */
public class SimpleData implements Data<String> {
    private String field;
    private SimpleDataTag simpleDataTag = new SimpleDataTag();

    @Override
    public DataTag getDataTag() {
        return simpleDataTag;
    }

    @Override
    public String getValue() {
        return null;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setSimpleDataTag(SimpleDataTag simpleDataTag) {
        this.simpleDataTag = simpleDataTag;
    }
}
