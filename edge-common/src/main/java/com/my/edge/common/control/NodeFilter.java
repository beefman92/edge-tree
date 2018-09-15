package com.my.edge.common.control;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface NodeFilter {
    /**
     * 假设NodeFilter所圈定的所有节点组成的集合为A，当前NodeMetadata所代表的所有节点组成的集合为B。那么A和B之间有五种关系
     * A包含于B且A和B不相等，-2
     * A和B有交集且互不包含，-1
     * A和B没有交集，0
     * B包含于A且A和B不相等，1
     * A与B相等，2
     * @param nodeMetadata node信息
     * @return 返回二者的关系
     */
    int relation(NodeMetadata nodeMetadata);

    boolean matches(NodeMetadata nodeMetadata);
}
