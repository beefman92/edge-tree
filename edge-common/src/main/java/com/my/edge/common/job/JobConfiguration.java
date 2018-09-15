package com.my.edge.common.job;

import java.util.ArrayList;
import java.util.List;

/**
 * Creator: Beefman
 * Date: 2018/9/14
 */
public class JobConfiguration {
    private String jobName;
    private String consumerClass;
    private String producerClass;
    private List<FileRecord> jars;
    private List<FileRecord> resources;

    public String getConsumerClass() {
        return consumerClass;
    }

    public void setConsumerClass(String consumerClass) {
        this.consumerClass = consumerClass;
    }

    public String getProducerClass() {
        return producerClass;
    }

    public void setProducerClass(String producerClass) {
        this.producerClass = producerClass;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public List<FileRecord> getJars() {
        return jars;
    }

    public void setJars(List<FileRecord> jars) {
        this.jars = jars;
    }

    public List<FileRecord> getResources() {
        return resources;
    }

    public void setResources(List<FileRecord> resources) {
        this.resources = resources;
    }

    public void addJar(FileRecord jar) {
        if (jars == null) {
            jars = new ArrayList<>();
        }
        jars.add(jar);
    }

    public void addResource(FileRecord resource) {
        if (resources == null) {
            resources = new ArrayList<>();
        }
        resources.add(resource);
    }
}
