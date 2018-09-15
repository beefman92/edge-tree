package com.my.edge.common.control.command;

import com.my.edge.common.control.ControlSignalType;
import com.my.edge.common.data.DataTag;

import java.util.Objects;
import java.util.Set;

/**
 * Creator: Beefman
 * Date: 2018/8/11
 */
public class StopSupplyData implements Command {
    private String id;
    private ControlSignalType controlSignalType = ControlSignalType.STOP_SUPPLY_DATA;
    private DataTag dataTag;
    /*
    当前节点向外传输数据的DataExport的id，它和远程节点对应的接收数据的DataImport的id是相同的
     */
    private Set<String> dataExportsId;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public ControlSignalType getControlSignalType() {
        return controlSignalType;
    }

    public void setControlSignalType(ControlSignalType controlSignalType) {
        this.controlSignalType = controlSignalType;
    }

    public DataTag getDataTag() {
        return dataTag;
    }

    public void setDataTag(DataTag dataTag) {
        this.dataTag = dataTag;
    }

    public Set<String> getDataExportsId() {
        return dataExportsId;
    }

    public void setDataExportsId(Set<String> dataExportsId) {
        this.dataExportsId = dataExportsId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StopSupplyData that = (StopSupplyData) o;
        return Objects.equals(id, that.id) &&
                controlSignalType == that.controlSignalType &&
                Objects.equals(dataTag, that.dataTag) &&
                Objects.equals(dataExportsId, that.dataExportsId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, controlSignalType, dataTag, dataExportsId);
    }
}
