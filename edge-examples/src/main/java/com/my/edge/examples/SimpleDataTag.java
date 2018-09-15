package com.my.edge.examples;

import com.my.edge.common.data.DataTag;

import java.util.Objects;

/**
 * Creator: Beefman
 * Date: 2018/9/14
 */
public class SimpleDataTag implements DataTag {
    private String field1;
    private String field2;

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public String getField2() {
        return field2;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleDataTag that = (SimpleDataTag) o;
        return Objects.equals(field1, that.field1) &&
                Objects.equals(field2, that.field2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field1, field2);
    }
}
