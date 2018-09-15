package com.my.edge.client;

import org.junit.Test;

/**
 * Creator: Beefman
 * Date: 2018/9/14
 */
public class ClientTest {
    @Test
    public void testRegisterJob() {
        String[] args = {"--name", "test-register-job",
                "--consumer", "com.my.edge.examples.SimpleConsumer",
                "--producer", "com.my.edge.examples.SimpleProducer",
                "--jars", "test-resources/edge-examples-1.0.jar"};
        Client client = new Client();
        client.registerJob(args);
    }
}
