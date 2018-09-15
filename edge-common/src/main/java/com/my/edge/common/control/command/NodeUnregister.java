package com.my.edge.common.control.command;

import com.my.edge.common.control.ControlSignalType;

import java.util.Objects;

public class NodeUnregister implements Command {
    private String id;
    private ControlSignalType controlSignalType = ControlSignalType.UNREGISTER;

    public void setId(String id) {
        this.id = id;
    }

    public void setControlSignalType(ControlSignalType controlSignalType) {
        this.controlSignalType = controlSignalType;
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
        NodeUnregister that = (NodeUnregister) o;
        return Objects.equals(id, that.id) &&
                controlSignalType == that.controlSignalType;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, controlSignalType);
    }
}
