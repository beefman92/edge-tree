package com.my.edge.server.job;

import com.my.edge.common.control.command.RunJob;
import com.my.edge.server.ServerHandler;
import com.my.edge.server.config.Configuration;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Creator: Beefman
 * Date: 2018/9/21
 */
public class JobHandler implements Runnable {
    private BlockingQueue<RunJob> runJobs = new ArrayBlockingQueue<>(10);
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private ServerHandler serverHandler;
    private Configuration configuration;

    public JobHandler(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setServerHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    @Override
    public void run() {
        while (true) {
            RunJob runJob = runJobs.peek();
            if (runJob != null) {
                String jobName = runJob.getJobName();
                Executor executor = new Executor(jobName, serverHandler, configuration, runJob.isConsumer());
                executorService.submit(executor);
                runJobs.remove();
            }
        }
    }

    public void addRunJob(RunJob runJob) {
        runJobs.add(runJob);
    }
}
