package com.my.edge.common.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface DataTag {

    boolean equals(Object object);

    int hashCode();
}
