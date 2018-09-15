package com.my.edge.common.control.command;

import com.my.edge.common.control.ControlSignalType;

import java.util.Objects;
import java.util.Set;

/**
 * Creator: Beefman
 * Date: 2018/8/11
 */
public class ReleaseDataChannel implements Command {
    private String id;
    private ControlSignalType controlSignalType = ControlSignalType.RELEASE_DATA_CHANNEL;
    /*
    当前节点接收数据的DataImport的id，与远端节点中对应的发送数据的DataExport的id相同
     */
    private Set<String> dataImportsId;

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

    public Set<String> getDataImportsId() {
        return dataImportsId;
    }

    public void setDataImportsId(Set<String> dataImportsId) {
        this.dataImportsId = dataImportsId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReleaseDataChannel that = (ReleaseDataChannel) o;
        return Objects.equals(id, that.id) &&
                controlSignalType == that.controlSignalType &&
                Objects.equals(dataImportsId, that.dataImportsId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, controlSignalType, dataImportsId);
    }
}
