package com.my.edge.server.data;

import java.net.SocketAddress;
import java.util.Objects;

/**
 * Creator: Beefman
 * Date: 2018/8/9
 */
public class DataImport {
    private String id;
    /*
    发送数据的远端节点
     */
    private SocketAddress source;

    public DataImport() {

    }

    public DataImport(String id, SocketAddress source) {
        this.id = id;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SocketAddress getSource() {
        return source;
    }

    public void setSource(SocketAddress source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataImport that = (DataImport) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, source);
    }
}
