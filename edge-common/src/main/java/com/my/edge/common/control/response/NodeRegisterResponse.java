package com.my.edge.common.control.response;

import com.my.edge.common.control.ControlSignalType;

import java.util.Objects;

/**
 * Creator: Beefman
 * Date: 2018/8/24
 */
public class NodeRegisterResponse implements Response {
    private String id;
    private String commandId;
    private ControlSignalType controlSignalType;

    public void setId(String id) {
        this.id = id;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public void setControlSignalType(ControlSignalType controlSignalType) {
        this.controlSignalType = controlSignalType;
    }

    @Override
    public String getCommandId() {
        return commandId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ControlSignalType getControlSignalType() {
        return controlSignalType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeRegisterResponse that = (NodeRegisterResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(commandId, that.commandId) &&
                controlSignalType == that.controlSignalType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, commandId, controlSignalType);
    }
}
