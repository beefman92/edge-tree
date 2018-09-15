package com.my.edge.common.control;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.my.edge.common.data.DataTag;
import com.my.edge.common.resolver.NodeMetadataResolver;

/**
 * NodeMetadata中记录的DataTag既包含当前节点所生成的数据的DataTag，也包含子节点生成数据的DataTag
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface NodeMetadata {
    void addDataTag(DataTag dataTag);

    void removeDataTag(DataTag dataTag);

    /**
     * 判断当前节点是否拥有dataTag类型的数据。这里的拥有既可以是当前节点生成的，也可以是当前节点的子节点生成的
     * @param dataTag dataTag
     * @return
     */
    boolean hasDataTag(DataTag dataTag);

    boolean equals(Object object);

    int hashCode();
}
