package com.my.edge.server;

import com.my.edge.server.config.NetworkTopology;
import com.my.edge.server.demo.DemoDataTag;
import com.my.edge.server.demo.DataGenerator;
import com.my.edge.common.network.NetworkManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class NodeManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private int port;
    private NetworkManager networkManager;
    private ServerHandler serverHandler;
    private NetworkTopology networkTopology;
    private DataGenerator dataGenerator;
    private boolean isDataGenerator;
    private boolean isTop;

    public NodeManager(int port, boolean isGenerator, boolean isTop) {
        this.port = port;
        this.networkTopology = new NetworkTopology();
        serverHandler = new ServerHandler(isTop);
        networkManager = new NetworkManager(this.port);
        serverHandler.setNetworkTopology(networkTopology);
        serverHandler.setNetworkManager(networkManager);
        dataGenerator = new DataGenerator(serverHandler, port);
        this.isDataGenerator = isGenerator;
        this.isTop = isTop;
    }

    public void initialize() {
        networkTopology.initialize();
        networkManager.initialize();
        if (isDataGenerator) {
            serverHandler.addGenerateData(DemoDataTag.DEMO_DATA_TAG_1);
        }
        serverHandler.initialize();
    }

    public void start() {
        try {
            logger.info("Starting NodeManager on port " + this.port);
            Thread networkManagerThread = new Thread(networkManager, "network-manager");
            networkManagerThread.start();
            serverHandler.start();
            if (isDataGenerator) {
                Thread dataGeneratorThread = new Thread(dataGenerator, "data-generator");
                dataGeneratorThread.start();
            }
        } catch (Exception e) {
            throw new RuntimeException("Starting NettyServer failed. ", e);
        }
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
