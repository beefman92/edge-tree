package com.my.edge.common.control.response;


import com.my.edge.common.control.ControlSignalType;

import java.util.Objects;

/**
 * Creator: Beefman
 * Date: 2018/9/21
 */
public class RunJobResponse implements Response {
    private String id;
    private String commandId;
    private ControlSignalType controlSignalType;
    private String jobName;
    private boolean succeeded;
    private String failureReason;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    @Override
    public ControlSignalType getControlSignalType() {
        return controlSignalType;
    }

    public void setControlSignalType(ControlSignalType controlSignalType) {
        this.controlSignalType = controlSignalType;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
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
        RunJobResponse that = (RunJobResponse) o;
        return succeeded == that.succeeded &&
                Objects.equals(id, that.id) &&
                Objects.equals(commandId, that.commandId) &&
                controlSignalType == that.controlSignalType &&
                Objects.equals(jobName, that.jobName) &&
                Objects.equals(failureReason, that.failureReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, commandId, controlSignalType, jobName, succeeded, failureReason);
    }
}
