package com.my.edge.common.control.command;

import com.my.edge.common.control.ControlSignalType;

import java.util.Objects;

/**
 * Creator: Beefman
 * Date: 2018/9/21
 */
public class RunJob implements Command {
    private String id;
    private ControlSignalType controlSignalType = ControlSignalType.RUN_JOB;
    private String jobName;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public ControlSignalType getControlSignalType() {
        return controlSignalType;
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
        RunJob runJob = (RunJob) o;
        return Objects.equals(id, runJob.id) &&
                controlSignalType == runJob.controlSignalType &&
                Objects.equals(jobName, runJob.jobName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, controlSignalType, jobName);
    }
}
