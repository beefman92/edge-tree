package com.my.edge.client;


import com.my.edge.common.control.ControlSignal;
import com.my.edge.common.control.command.Command;
import com.my.edge.common.control.command.RegisterJob;
import com.my.edge.common.control.response.RegisterJobResponse;
import com.my.edge.common.entity.Tuple2;
import com.my.edge.common.job.FileRecord;
import com.my.edge.common.job.JobConfiguration;
import com.my.edge.common.network.NetworkManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creator: Beefman
 * Date: 2018/9/13
 */
public class Client {
    private NetworkManager networkManager;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Client() {
        networkManager = new NetworkManager(0, NetworkManager.CLIENT_ONLY);
        networkManager.initialize();
        networkManager.run();
    }

    private Config parse(String[] args) {
        Map<String, String> parameters = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            String key = args[i];
            String value = args[i + 1];
            parameters.put(key, value);
        }
        Config config = new Config();
        String name = getValue(parameters, "--name", false);
        String consumer = getValue(parameters, "--consumer", false);
        String producer = getValue(parameters, "--producer", false);
        String jars = getValue(parameters, "--jars", false);
        String resources = getValue(parameters, "--resources", true);
        config.setJobName(name);
        config.setConsumerClass(consumer);
        config.setProducerClass(producer);
        String[] jarsPath = jars.split(",");
        List<File> jarsFile = new ArrayList<>();
        for (String jarPath: jarsPath) {
            File file = new File(jarPath);
            if (file.exists()) {
                collectFileRecursively(file, jarsFile, ".jar");
            }
        }
        config.setJarFiles(jarsFile);
        if (StringUtils.isNotBlank(resources)) {
            String[] resourcesPath = resources.split(",");
            List<File> resourcesFile = new ArrayList<>();
            for (String resourcePath: resourcesPath) {
                File file = new File(resourcePath);
                if (file.exists()) {
                    collectFileRecursively(file, resourcesFile, null);
                }
            }
            config.setResourceFiles(resourcesFile);
        }
        return config;
    }

    private JobConfiguration constructJobConfiguration(Config config) {
        JobConfiguration jobConfiguration = new JobConfiguration();
        jobConfiguration.setJobName(config.getJobName());
        jobConfiguration.setProducerClass(config.getProducerClass());
        jobConfiguration.setConsumerClass(config.getConsumerClass());
        for (File jarFile: config.getJarFiles()) {
            logger.info("Adding jar " + jarFile.getAbsolutePath() + " to JobConfiguration. ");
            FileRecord fileRecord = generateFileRecord(jarFile);
            jobConfiguration.addJar(fileRecord);
        }
        if (config.getResourceFiles() != null) {
            for (File resourceFile: config.getResourceFiles()) {
                logger.info("Adding resource file " + resourceFile.getAbsolutePath() + " to JobConfiguration. ");
                FileRecord fileRecord = generateFileRecord(resourceFile);
                jobConfiguration.addResource(fileRecord);
            }
        }
        return jobConfiguration;
    }

    private FileRecord generateFileRecord(File file) {
        FileRecord fileRecord = new FileRecord();
        fileRecord.setFileName(file.getName());
        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
            fileRecord.setFileContent(output.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Creating FileRecord for file " + file.getAbsolutePath() + " failed. ", e);
        }
        return fileRecord;
    }

    public void registerJob(String[] args) {
        Config config = parse(args);
        JobConfiguration jobConfiguration = constructJobConfiguration(config);
        RegisterJob registerJob = Command.newJobRegister(jobConfiguration);
        String nodeAddress = System.getenv("EDGE_SERVER");
        String[] parts = nodeAddress.split(":");
        SocketAddress address = new InetSocketAddress(parts[0], Integer.valueOf(parts[1]));
        logger.info("Registering job " + jobConfiguration.getJobName() + " to node " + address);
        networkManager.sendCommand(address, registerJob);
        logger.info("Waiting response... ");
        Tuple2<SocketAddress, ControlSignal> tuple = networkManager.fetchControlSignal();
        if (!address.equals(tuple.getValue1()) || tuple.getValue2().getClass() != RegisterJobResponse.class) {
            throw new RuntimeException("嗯?!");
        }
        RegisterJobResponse response = (RegisterJobResponse) tuple.getValue2();
        if (response.isSucceeded()) {
            logger.info("Registering job " + jobConfiguration.getJobName() + " succeeded. ");
        } else {
            logger.info("Registering job " + jobConfiguration.getJobName() + " failed with reason: " +
                    response.getFailureReason());
        }
    }

    private static void collectFileRecursively(File file, List<File> files, String suffix) {
        if (file.isFile()) {
            if (StringUtils.isNotBlank(suffix)) {
                if (file.getName().endsWith(suffix)) {
                    files.add(file);
                }
            } else {
                files.add(file);
            }
        } else {
            File[] subFiles = file.listFiles();
            for (File subFile: subFiles) {
                collectFileRecursively(subFile, files, suffix);
            }
        }
    }

    private static String getValue(Map<String, String> parameters, String key, boolean optional) {
        String value = parameters.get(key);
        if (StringUtils.isBlank(value) && !optional) {
            throw new RuntimeException("Parameter " + key + " is not assigned. ");
        }
        return value;
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.registerJob(args);
    }


}
