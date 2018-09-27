package com.my.edge.examples;

import com.my.edge.common.control.NodeFilter;
import com.my.edge.common.control.NodeMetadata;

public class DemoNodeFilter implements NodeFilter {
    @Override
    public boolean matches(NodeMetadata nodeMetadata) {
        return true;
    }

    @Override
    public int relation(NodeMetadata nodeMetadata) {
        return 1;
    }

}
