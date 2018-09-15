package com.my.edge.server.config;

import com.my.edge.common.data.DataTag;
import com.my.edge.common.control.NodeMetadata;
import com.my.edge.server.util.JsonSerializer;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;

public class NetworkTopology {
    private Map<SocketAddress, NodeMetadata> parents;
    private Map<SocketAddress, NodeMetadata> siblings;
    private Map<SocketAddress, NodeMetadata> children;

    public NetworkTopology() {
        this.parents = new HashMap<>();
        this.siblings = new HashMap<>();
        this.children = new HashMap<>();
    }

    public void initialize() {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("network-topology.json")) {
            NetworkTopologyConfig config = JsonSerializer.objectMapper.readValue(inputStream, NetworkTopologyConfig.class);
            for (NetworkTopologyConfig.NetworkTopologyRecord record: config.getParents()) {
                this.parents.put(new InetSocketAddress(record.getHost(), record.getPort()), null);
            }
        } catch (Exception e) {
            throw new RuntimeException("Initializing network topology failed. ", e);
        }

    }

    public void addChild(SocketAddress child, NodeMetadata nodeMetadata) {
        children.put(child, nodeMetadata);
    }

    public void addSibling(SocketAddress sibling, NodeMetadata nodeMetadata) {
        siblings.put(sibling, nodeMetadata);
    }

    public void removeChild(SocketAddress socketAddress) {
        children.remove(socketAddress);
    }

    public void removeSibling(SocketAddress socketAddress) {
        siblings.remove(socketAddress);
    }

    public void addParent(SocketAddress parent, NodeMetadata nodeMetadata) {
        parents.put(parent, nodeMetadata);
    }

    public void removeParent(SocketAddress socketAddress) {
        parents.remove(socketAddress);
    }

    public Iterator<Map.Entry<SocketAddress, NodeMetadata>> getChildren() {
        return children.entrySet().iterator();
    }

    public Iterator<Map.Entry<SocketAddress, NodeMetadata>> getParents() {
        return parents.entrySet().iterator();
    }

    public Iterator<SocketAddress> getParentsAddress() {
        return parents.keySet().iterator();
    }

    public boolean isParent(SocketAddress socketAddress) {
        return parents.containsKey(socketAddress);
    }

    public boolean isChildren(SocketAddress socketAddress) {
        return children.containsKey(socketAddress);
    }

    public void addChildDataTag(SocketAddress child, DataTag dataTag) {
        NodeMetadata childMetadata = children.get(child);
        childMetadata.addDataTag(dataTag);
    }

    public void removeChildDataTag(SocketAddress child, DataTag dataTag) {
        NodeMetadata childMetadata = children.get(child);
        childMetadata.removeDataTag(dataTag);
    }
}
