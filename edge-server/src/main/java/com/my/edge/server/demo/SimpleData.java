package com.my.edge.server.demo;


import com.my.edge.common.data.Data;
import com.my.edge.common.data.DataTag;

/**
 * Creator: Beefman
 * Date: 2018/8/31
 */
public class SimpleData implements Data<String> {
    private DataTag dataTag;
    private String value;

    @Override
    public DataTag getDataTag() {
        return dataTag;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setDataTag(DataTag dataTag) {
        this.dataTag = dataTag;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
