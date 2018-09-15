package com.my.edge.common.control.command;

import com.my.edge.common.control.ControlSignalType;
import com.my.edge.common.job.JobConfiguration;

import java.util.Objects;

/**
 * Creator: Beefman
 * Date: 2018/9/14
 */
public class RegisterJob implements Command {
    private String id;
    private ControlSignalType controlSignalType;
    private JobConfiguration jobConfiguration;

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

    public JobConfiguration getJobConfiguration() {
        return jobConfiguration;
    }

    public void setJobConfiguration(JobConfiguration jobConfiguration) {
        this.jobConfiguration = jobConfiguration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisterJob that = (RegisterJob) o;
        return Objects.equals(id, that.id) &&
                controlSignalType == that.controlSignalType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, controlSignalType);
    }
}
