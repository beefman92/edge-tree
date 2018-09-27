package com.my.edge.server;

import com.my.edge.common.control.command.RunJob;
import com.my.edge.examples.DemoDataTag;
import com.my.edge.server.config.Configuration;
import com.my.edge.server.config.NetworkTopology;
import com.my.edge.common.network.NetworkManager;
import com.my.edge.server.job.JobHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class NodeManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private int port;
    private Configuration configuration;
    private NetworkManager networkManager;
    private ServerHandler serverHandler;
    private JobHandler jobHandler;
    private NetworkTopology networkTopology;
    private boolean isDataGenerator;
    private boolean isTop;

    public NodeManager(int port, boolean isGenerator, boolean isTop) {
        this.port = port;
        this.networkTopology = new NetworkTopology();
        this.configuration = new Configuration();
        serverHandler = new ServerHandler(isTop);
        networkManager = new NetworkManager(this.port);
        serverHandler.setNetworkTopology(networkTopology);
        serverHandler.setNetworkManager(networkManager);
        serverHandler.setNodeManager(this);
        jobHandler = new JobHandler(configuration);
        this.isDataGenerator = isGenerator;
        this.isTop = isTop;
    }

    public void initialize() {
        networkTopology.initialize();
        networkManager.initialize();
        // demo code start
        if (isDataGenerator) {
            serverHandler.addGenerateData(DemoDataTag.DEMO_DATA_TAG_1);
        }
        // demo code end
        serverHandler.initialize();
        jobHandler.setServerHandler(this.serverHandler);
        serverHandler.setJobHandler(jobHandler);
    }

    public void start() {
        try {
            logger.info("Starting NodeManager on port " + this.port);
            Thread networkManagerThread = new Thread(networkManager, "network-manager");
            networkManagerThread.start();
            serverHandler.start();
            Thread jobHandlerThread = new Thread(jobHandler, "job-handler");
            jobHandlerThread.start();
        } catch (Exception e) {
            throw new RuntimeException("Starting NettyServer failed. ", e);
        }
    }

    public void addRunJob(RunJob runJob) {
        jobHandler.addRunJob(runJob);
    }

    public static void main(String[] args) {
        Map<String, String> arguments = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            String key = args[i];
            String value = args[i + 1];
            arguments.put(key, value);
        }
        int port = Integer.valueOf(arguments.get("--port"));
        boolean isDataGenerator = getBooleanValue(arguments, "--isDataGenerator", false);
        boolean isTop = getBooleanValue(arguments, "--isTop", false);
        NodeManager nodeManager = new NodeManager(port, isDataGenerator, isTop);
        nodeManager.initialize();
        nodeManager.start();
    }

    private static boolean getBooleanValue(Map<String, String> map, String key, boolean defaultValue) {
        String temp = map.get(key);
        if (StringUtils.isNoneBlank(temp)) {
            return Boolean.valueOf(temp);
        }
        return defaultValue;
    }
}
