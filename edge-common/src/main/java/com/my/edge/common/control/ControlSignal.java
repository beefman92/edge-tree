package com.my.edge.common.control;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface ControlSignal {

    String getId();

    ControlSignalType getControlSignalType();

    boolean equals(Object object);

    int hashCode();
}