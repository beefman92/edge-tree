package com.my.edge.server.job;

import com.my.edge.common.job.JobConfiguration;
import com.my.edge.server.ServerHandler;

/**
 * Creator: Beefman
 * Date: 2018/9/15
 */
public class Executor implements Runnable {
    private String jobName;
    private ServerHandler serverHandler;
    private JobConfiguration jobConfiguration;

    public Executor(String jobName, ServerHandler serverHandler) {
        this.jobName = jobName;
        this.serverHandler = serverHandler;
    }

    @Override
    public void run() {

    }
}
