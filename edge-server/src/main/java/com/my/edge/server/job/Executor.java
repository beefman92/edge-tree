package com.my.edge.server.job;

import com.my.edge.common.job.Consumer;
import com.my.edge.common.job.FileRecord;
import com.my.edge.common.job.JobConfiguration;
import com.my.edge.common.job.Producer;
import com.my.edge.server.ServerHandler;
import com.my.edge.server.config.Configuration;

import javax.management.RuntimeErrorException;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Creator: Beefman
 * Date: 2018/9/15
 */
public class Executor implements Runnable {
    private String jobName;
    private ServerHandler serverHandler;
    private Configuration configuration;
    private JobConfiguration jobConfiguration;
    private File jobDir;
    private URLClassLoader urlClassLoader;
    private boolean consumer;

    public Executor(String jobName, ServerHandler serverHandler, Configuration configuration, boolean consumer) {
        this.jobName = jobName;
        this.serverHandler = serverHandler;
        this.configuration = configuration;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        getJob();
        try {
            if (consumer) {
                Class<?> consumerClass = Class.forName(jobConfiguration.getConsumerClass(), true, urlClassLoader);
                Consumer consumer = (Consumer) consumerClass.newInstance();
                consumer.consume(null, null);
            } else {
                Class<?> producerClass = Class.forName(jobConfiguration.getProducerClass(), true, urlClassLoader);
                Producer producer = (Producer) producerClass.newInstance();
                producer.produce(null, null );
            }
        } catch (Exception e) {
            throw new RuntimeException("Running job " + jobConfiguration.getJobName() + " failed. ", e);
        }
    }

    private void getJob() {
        jobConfiguration = serverHandler.requestJobConfiguration(this.jobName, true);
        String tmpDir = configuration.getTmpDir();
        String jobDirPath = jobName + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString();
        jobDir = new File(tmpDir, jobDirPath);
        if (!jobDir.mkdirs()) {
            throw new RuntimeException("Creating temp directory for job " + jobName + " failed. ");
        }
        List<FileRecord> files = jobConfiguration.getJars();
        List<URL> fileURLs = new ArrayList<>();
        try {
            byte[] buffer = new byte[1024];
            for (FileRecord fileRecord : files) {
                String fileName = fileRecord.getFileName();
                File file = new File(jobDir, fileName);
                if (!file.createNewFile()) {
                    throw new RuntimeException("Cannot create file under directory " + jobDir.getAbsolutePath());
                }
                try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileRecord.getFileContent());
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
                    int length;
                    while ((length = inputStream.read(buffer)) >= 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
                URL url = file.toURI().toURL();
                fileURLs.add(url);
            }
        } catch (Exception e) {
            throw new RuntimeException("Writing job files to temp directory " + jobDir.getAbsolutePath() + " failed. ", e);
        }
        this.urlClassLoader = new URLClassLoader(fileURLs.toArray(new URL[0]), this.getClass().getClassLoader());
    }
}
