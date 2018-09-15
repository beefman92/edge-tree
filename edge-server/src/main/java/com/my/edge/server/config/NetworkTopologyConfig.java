package com.my.edge.server.config;

import java.util.List;

/**
 * Creator: Beefman
 * Date: 2018/8/2
 */
public class NetworkTopologyConfig {
    private List<NetworkTopologyRecord> parents;
    private List<NetworkTopologyRecord> siblings;
    private List<NetworkTopologyRecord> children;

    public NetworkTopologyConfig() {

    }

    public List<NetworkTopologyRecord> getParents() {
        return parents;
    }

    public void setParents(List<NetworkTopologyRecord> parents) {
        this.parents = parents;
    }

    public List<NetworkTopologyRecord> getSiblings() {
        return siblings;
    }

    public void setSiblings(List<NetworkTopologyRecord> siblings) {
        this.siblings = siblings;
    }

    public List<NetworkTopologyRecord> getChildren() {
        return children;
    }

    public void setChildren(List<NetworkTopologyRecord> children) {
        this.children = children;
    }

    public static class NetworkTopologyRecord {
        String host;
        int port;

        public NetworkTopologyRecord() {

        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
