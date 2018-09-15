package com.my.edge.common.control.command;

import com.my.edge.common.control.ControlSignalType;
import com.my.edge.common.control.NodeMetadata;

import java.util.Objects;

public class NodeRegister implements Command {
    private String id;
    private ControlSignalType controlSignalType = ControlSignalType.REGISTER;
    private NodeMetadata nodeMetadata;

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

    public NodeMetadata getNodeMetadata() {
        return nodeMetadata;
    }

    public void setNodeMetadata(NodeMetadata nodeMetadata) {
        this.nodeMetadata = nodeMetadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeRegister that = (NodeRegister) o;
        return Objects.equals(id, that.id) &&
                controlSignalType == that.controlSignalType &&
                Objects.equals(nodeMetadata, that.nodeMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, controlSignalType, nodeMetadata);
    }
}
