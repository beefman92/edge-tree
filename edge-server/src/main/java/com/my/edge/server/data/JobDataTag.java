package com.my.edge.server.data;

import com.my.edge.common.data.DataTag;

import java.util.Objects;

/**
 * Creator: Beefman
 * Date: 2018/9/14
 */
public class JobDataTag implements DataTag {
    private String jobName;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobDataTag that = (JobDataTag) o;
        return Objects.equals(jobName, that.jobName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobName);
    }
}
