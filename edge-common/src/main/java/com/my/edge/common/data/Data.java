package com.my.edge.common.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface Data<T extends Object> {
    DataTag getDataTag();
    long getTimestamp();
    void setTimestamp(long timestamp);
    T getValue();
}
