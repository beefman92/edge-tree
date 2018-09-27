package com.my.edge.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Creator: Beefman
 * Date: 2018/9/14
 */
public class RegisterConfig {
    private String jobName;
    private List<File> jarFiles;
    private List<File> resourceFiles;
    private String producerClass;
    private String consumerClass;
    private String dataTagClass;
    private String nodeFilterClass;

    public RegisterConfig() {
        this.jarFiles = new ArrayList<>();
        this.resourceFiles = new ArrayList<>();
    }

    public List<File> getJarFiles() {
        return jarFiles;
    }

    public void setJarFiles(List<File> jarFiles) {
        this.jarFiles = jarFiles;
    }

    public List<File> getResourceFiles() {
        return resourceFiles;
    }

    public void setResourceFiles(List<File> resourceFiles) {
        this.resourceFiles = resourceFiles;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getProducerClass() {
        return producerClass;
    }

    public void setProducerClass(String producerClass) {
        this.producerClass = producerClass;
    }

    public String getConsumerClass() {
        return consumerClass;
    }

    public void setConsumerClass(String consumerClass) {
        this.consumerClass = consumerClass;
    }

    public void addJarFile(File jarFile) {
        if (jarFiles == null) {
            jarFiles = new ArrayList<>();
        }
        jarFiles.add(jarFile);
    }

    public void addResourceFile(File resourceFile) {
        if (resourceFiles == null) {
            resourceFiles = new ArrayList<>();
        }
        resourceFiles.add(resourceFile);
    }

    public String getDataTagClass() {
        return dataTagClass;
    }

    public void setDataTagClass(String dataTagClass) {
        this.dataTagClass = dataTagClass;
    }

    public String getNodeFilterClass() {
        return nodeFilterClass;
    }

    public void setNodeFilterClass(String nodeFilterClass) {
        this.nodeFilterClass = nodeFilterClass;
    }
}
