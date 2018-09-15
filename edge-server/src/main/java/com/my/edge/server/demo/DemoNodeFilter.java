package com.my.edge.server.demo;

import com.my.edge.common.control.NodeFilter;
import com.my.edge.common.control.NodeMetadata;

public class DemoNodeFilter implements NodeFilter {
    @Override
    public boolean matches(NodeMetadata nodeMetadata) {
        if (nodeMetadata.getClass() != DemoNodeMetadata.class) {
            return false;
        }
        return true;
    }

    @Override
    public int relation(NodeMetadata nodeMetadata) {
        return 1;
    }

}
