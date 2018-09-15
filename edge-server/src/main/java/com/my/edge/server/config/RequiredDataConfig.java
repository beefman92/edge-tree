package com.my.edge.server.config;


import com.my.edge.common.data.DataTag;

import java.util.Set;

/**
 * Creator: Beefman
 * Date: 2018/8/2
 */
public class RequiredDataConfig {
    private Set<DataTag> requiredDataTags;

    public Set<DataTag> getRequiredDataTags() {
        return requiredDataTags;
    }

    public void setRequiredDataTags(Set<DataTag> requiredDataTags) {
        this.requiredDataTags = requiredDataTags;
    }
}
