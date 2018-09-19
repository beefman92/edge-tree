package com.my.edge.common.control.response;

import com.my.edge.common.control.ControlSignalType;
import com.my.edge.common.job.JobConfiguration;

import java.util.Objects;

/**
 * Creator: Beefman
 * Date: 2018/9/15
 */
public class RequestJobResponse implements Response {
    private String id;
    private String commandId;
    private ControlSignalType controlSignalType;
    private boolean hasJob;
    private JobConfiguration jobConfiguration;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public ControlSignalType getControlSignalType() {
        return controlSignalType;
    }

    public void setControlSignalType(ControlSignalType controlSignalType) {
        this.controlSignalType = controlSignalType;
    }

    public JobConfiguration getJobConfiguration() {
        return jobConfiguration;
    }

    public void setJobConfiguration(JobConfiguration jobConfiguration) {
        this.jobConfiguration = jobConfiguration;
    }

    public boolean isHasJob() {
        return hasJob;
    }

    public void setHasJob(boolean hasJob) {
        this.hasJob = hasJob;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestJobResponse that = (RequestJobResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(commandId, that.commandId) &&
                controlSignalType == that.controlSignalType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, commandId, controlSignalType);
    }
}
