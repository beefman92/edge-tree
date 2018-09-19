package com.my.edge.common.control.command;

import com.my.edge.common.control.ControlSignalType;

import java.util.Objects;

import static com.my.edge.common.control.ControlSignalType.REQUEST_JOB;

/**
 * Creator: Beefman
 * Date: 2018/9/15
 */
public class RequestJob implements Command {
    private String id;
    private ControlSignalType controlSignalType = REQUEST_JOB;
    private String jobName;

    @Override
    public String getId() {
        return null;
    }

    @Override
    public ControlSignalType getControlSignalType() {
        return null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setControlSignalType(ControlSignalType controlSignalType) {
        this.controlSignalType = controlSignalType;
    }

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
        RequestJob that = (RequestJob) o;
        return Objects.equals(id, that.id) &&
                controlSignalType == that.controlSignalType &&
                Objects.equals(jobName, that.jobName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, controlSignalType, jobName);
    }
}
