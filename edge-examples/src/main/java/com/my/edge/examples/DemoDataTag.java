package com.my.edge.examples;


import com.my.edge.common.data.DataTag;

import java.util.Objects;

/**
 * Creator: Beefman
 * Date: 2018/8/30
 */
public class DemoDataTag implements DataTag {
    public static final DemoDataTag DEMO_DATA_TAG_1 = new DemoDataTag();
    static {
        DEMO_DATA_TAG_1.setDataTypeInfo("TEST_DATA");
    }

    private String dataTypeInfo = "TEST_DATA";

    public String getDataTypeInfo() {
        return dataTypeInfo;
    }

    public void setDataTypeInfo(String dataTypeInfo) {
        this.dataTypeInfo = dataTypeInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DemoDataTag that = (DemoDataTag) o;
        return Objects.equals(dataTypeInfo, that.dataTypeInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataTypeInfo);
    }
}
