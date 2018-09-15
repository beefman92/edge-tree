package com.my.edge.common.control.command;

import com.my.edge.common.control.ControlSignalType;
import com.my.edge.common.data.DataTag;

/**
 * Creator: Beefman
 * Date: 2018/8/11
 */
public class SupplyData implements Command {
    private String id;
    private ControlSignalType controlSignalType = ControlSignalType.SUPPLY_DATA;
    private DataTag dataTag;

    public SupplyData() {

    }

    public SupplyData(String id, ControlSignalType controlSignalType, DataTag dataTag) {
        this.id = id;
        this.controlSignalType = controlSignalType;
        this.dataTag = dataTag;
    }

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
}
