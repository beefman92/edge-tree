package com.my.edge.server.demo;

import com.my.edge.common.control.NodeMetadata;
import com.my.edge.common.data.DataTag;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DemoNodeMetadata implements NodeMetadata {
    private String type;
    private String location;
    private Set<DataTag> accessibleDataTags;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Set<DataTag> getAccessibleDataTags() {
        return accessibleDataTags;
    }

    public void setAccessibleDataTags(Set<DataTag> accessibleDataTags) {
        this.accessibleDataTags = accessibleDataTags;
    }

    @Override
    public void addDataTag(DataTag dataTag) {
        if (accessibleDataTags == null) {
            accessibleDataTags = new HashSet<>();
        }
        accessibleDataTags.add(dataTag);
    }

    @Override
    public void removeDataTag(DataTag dataTag) {
        if (accessibleDataTags != null) {
            accessibleDataTags.remove(dataTag);
        }
    }

    @Override
    public boolean hasDataTag(DataTag dataTag) {
        if (accessibleDataTags != null) {
            return accessibleDataTags.contains(dataTag);
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DemoNodeMetadata that = (DemoNodeMetadata) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {

        return Objects.hash(type, location);
    }
}
