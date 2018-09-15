package com.my.edge.common.control.response;

import com.my.edge.common.control.ControlSignalType;

import java.util.Objects;

/**
 * Creator: Beefman
 * Date: 2018/8/24
 */
public class StopSupplyDataResponse implements Response {
    private String id;
    private String commandId;
    private ControlSignalType controlSignalType;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    @Override
    public ControlSignalType getControlSignalType() {
        return controlSignalType;
    }

    public void setControlSignalType(ControlSignalType controlSignalType) {
        this.controlSignalType = controlSignalType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StopSupplyDataResponse that = (StopSupplyDataResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(commandId, that.commandId) &&
                controlSignalType == that.controlSignalType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, commandId, controlSignalType);
    }
}
