package com.my.edge.common.control.response;

import com.my.edge.common.control.ControlSignalType;

import java.util.Objects;

/**
 * Creator: Beefman
 * Date: 2018/8/24
 */
public class RequestDataResponse implements Response {
    private String id;
    private String commandId;
    private ControlSignalType controlSignalType;
    private boolean hasData;
    /*
    对于生成Response的节点，dataExportId是自己向外发送数据的DataExport的id；
    对于接收Repsonse的节点，dataExportId是自己接收数据的DataImport的id
     */
    private String dataExportId;

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

    public boolean isHasData() {
        return hasData;
    }

    public void setHasData(boolean hasData) {
        this.hasData = hasData;
    }

    public String getDataExportId() {
        return dataExportId;
    }

    public void setDataExportId(String dataExportId) {
        this.dataExportId = dataExportId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestDataResponse that = (RequestDataResponse) o;
        return hasData == that.hasData &&
                Objects.equals(id, that.id) &&
                Objects.equals(commandId, that.commandId) &&
                controlSignalType == that.controlSignalType &&
                Objects.equals(dataExportId, that.dataExportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, commandId, controlSignalType, hasData, dataExportId);
    }
}
