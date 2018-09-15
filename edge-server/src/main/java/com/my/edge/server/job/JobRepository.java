package com.my.edge.server.job;

import com.my.edge.common.job.JobConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Creator: Beefman
 * Date: 2018/9/14
 */
public class JobRepository {
    private Map<String, JobConfiguration> jobMap;

    public JobRepository() {
        this.jobMap = new HashMap<>();
    }

    public void addJobConfiguration(JobConfiguration jobConfiguration) {
        String jobName = jobConfiguration.getJobName();
        jobMap.put(jobName, jobConfiguration);
    }

    public JobConfiguration getJobConfiguration(String jobName) {
        return jobMap.get(jobName);
    }
}
