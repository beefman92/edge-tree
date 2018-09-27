package com.my.edge.common.data;

import java.util.List;

/**
 * Creator: Beefman
 * Date: 2018/8/31
 */
public class DataWrapper {
    private Data data;
    /*
    同一个DataChannel在发送端和接收端的id是一致的，因此上述情况成立
    对于发送DataWrapper的节点，dataExportsId记录发送数据的DataExport的id
    对于接收DataWrapper的节点，dataExportsId记录接收数据的DataImport的id
     */
    private List<String> dataExportsId;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public List<String> getDataExportsId() {
        return dataExportsId;
    }

    public void setDataExportsId(List<String> dataExportsId) {
        this.dataExportsId = dataExportsId;
    }
}
