package com.my.edge.common.control.command;

import com.my.edge.common.control.ControlSignalType;
import com.my.edge.common.control.NodeFilter;
import com.my.edge.common.data.DataTag;

import java.util.Objects;

public class RequestData implements Command {
    private String id;
    private ControlSignalType controlSignalType;
    private DataTag dataTag;
    private NodeFilter nodeFilter;
    private String importId;  //发送请求的节点的import id

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

    public DataTag getDataTag() {
        return dataTag;
    }

    public void setDataTag(DataTag dataTag) {
        this.dataTag = dataTag;
    }

    public NodeFilter getNodeFilter() {
        return nodeFilter;
    }

    public void setNodeFilter(NodeFilter nodeFilter) {
        this.nodeFilter = nodeFilter;
    }

    public String getImportId() {
        return importId;
    }

    public void setImportId(String importId) {
        this.importId = importId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestData that = (RequestData) o;
        return Objects.equals(id, that.id) &&
                controlSignalType == that.controlSignalType &&
                Objects.equals(dataTag, that.dataTag) &&
                Objects.equals(nodeFilter, that.nodeFilter) &&
                Objects.equals(importId, that.importId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, controlSignalType, dataTag, nodeFilter, importId);
    }
}
