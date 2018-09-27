package com.my.edge.server.job;

import com.my.edge.common.control.NodeFilter;
import com.my.edge.common.data.Data;
import com.my.edge.common.data.DataTag;
import com.my.edge.common.entity.Tuple2;
import com.my.edge.common.job.Consumer;
import com.my.edge.common.job.FileRecord;
import com.my.edge.common.job.JobConfiguration;
import com.my.edge.common.job.Producer;
import com.my.edge.server.ServerHandler;
import com.my.edge.server.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Creator: Beefman
 * Date: 2018/9/15
 */
public class Executor implements Runnable {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
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
                logger.info("Starting consumer task " + jobConfiguration.getConsumerClass() + " of job " + jobName);
                Class<?> consumerClass = Class.forName(jobConfiguration.getConsumerClass(), true, urlClassLoader);
                Consumer consumer = (Consumer) consumerClass.newInstance();
                Class<?> dataTagClass = Class.forName(jobConfiguration.getDataTagClass(), true, urlClassLoader);
                DataTag dataTag = (DataTag) dataTagClass.newInstance();
                Class<?> nodeFilterClass = Class.forName(jobConfiguration.getNodeFilterClass(), true, urlClassLoader);
                NodeFilter nodeFilter = (NodeFilter) nodeFilterClass.newInstance();
                WindowTransmitter windowTransmitter = new WindowTransmitter(serverHandler, dataTag, nodeFilter);
                windowTransmitter.prepareData();
                while (true) {
                    Tuple2<DataTag, Iterator<Data>> res = windowTransmitter.fetchData();
                    if (res != null) {
                        consumer.consume(res.getValue1(), res.getValue2());
                    }
                }
            } else {
                logger.info("Starting producer task " + jobConfiguration.getProducerClass() + " of job " + jobName);
                Class<?> producerClass = Class.forName(jobConfiguration.getProducerClass(), true, urlClassLoader);
                Producer producer = (Producer) producerClass.newInstance();
                while (true) {
                    Tuple2<? extends DataTag, ? extends Data> result = producer.produce(null, null);
                    serverHandler.addData(null, result.getValue2());
                }
            }
        } catch (Throwable e) {
            logger.error("Running job " + jobName + " failed. ", e);
        } finally {
            try {
                urlClassLoader.close();
            } catch (Throwable t) {
                logger.warn("Closing class loader of job " + jobName + " encounters exception. ", t);
            }
            recursivelyDelete(jobDir);
            logger = null;
            jobName = null;
            serverHandler = null;
            configuration = null;
            jobConfiguration = null;
            jobDir = null;
            urlClassLoader = null;
        }
    }

    private void recursivelyDelete(File file) {
        if (file != null) {
            if (file.isFile()) {
                if (!file.delete()) {
                    logger.warn("Delete file " + file.getAbsolutePath() + " failed. ");
                } else {
                    logger.debug("Delete temporary file " + file.getAbsolutePath());
                }
            } else {
                File[] subFiles = file.listFiles();
                for (File subFile: subFiles) {
                    recursivelyDelete(subFile);
                }
                if (!file.delete()) {
                    logger.warn("Delete directory " + file.getAbsolutePath() + " failed. ");
                } else {
                    logger.debug("Delete temporary file " + file.getAbsolutePath());
                }
            }
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
                logger.debug("Create temporary file " + file.getAbsolutePath());
                URL url = file.toURI().toURL();
                fileURLs.add(url);
            }
        } catch (Exception e) {
            throw new RuntimeException("Writing job files to temp directory " + jobDir.getAbsolutePath() + " failed. ", e);
        }
        this.urlClassLoader = new URLClassLoader(fileURLs.toArray(new URL[0]), this.getClass().getClassLoader());
    }
}
