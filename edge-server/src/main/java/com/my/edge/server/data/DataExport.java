package com.my.edge.server.data;

import java.net.SocketAddress;
import java.util.Objects;

/**
 * Creator: Beefman
 * Date: 2018/8/9
 */
public class DataExport {
    private String id;
    /*
    接收数据的远端节点
     */
    private SocketAddress target;

    public DataExport() {

    }

    public DataExport(String id, SocketAddress target) {
        this.id = id;
        this.target = target;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SocketAddress getTarget() {
        return target;
    }

    public void setTarget(SocketAddress target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataExport that = (DataExport) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, target);
    }
}
