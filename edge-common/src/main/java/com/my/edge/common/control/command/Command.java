package com.my.edge.common.control.command;

import com.my.edge.common.control.ControlSignal;
import com.my.edge.common.control.ControlSignalType;
import com.my.edge.common.control.NodeFilter;
import com.my.edge.common.control.NodeMetadata;
import com.my.edge.common.data.DataTag;
import com.my.edge.common.job.JobConfiguration;

import java.util.Set;
import java.util.UUID;

/**
 * Creator: Beefman
 * Date: 2018/7/27
 */
public interface Command extends ControlSignal {
    static NodeRegister newRegisterCommand(NodeMetadata nodeMetadata) {
        NodeRegister nodeRegister = new NodeRegister();
        nodeRegister.setId(UUID.randomUUID().toString());
        nodeRegister.setControlSignalType(ControlSignalType.REGISTER);
        nodeRegister.setNodeMetadata(nodeMetadata);
        return nodeRegister;
    }

    static RequestData newRequestDataCommand(DataTag dataTag, NodeFilter nodeFilter, String importId) {
        RequestData requestData = new RequestData();
        requestData.setId(UUID.randomUUID().toString());
        requestData.setControlSignalType(ControlSignalType.REQUEST_DATA);
        requestData.setDataTag(dataTag);
        requestData.setNodeFilter(nodeFilter);
        requestData.setImportId(importId);
        return requestData;
    }

    static RequestData newRequesDataCommand(String id, DataTag dataTag, NodeFilter nodeFilter, String importId) {
        RequestData requestData = new RequestData();
        requestData.setId(id);
        requestData.setControlSignalType(ControlSignalType.REQUEST_DATA);
        requestData.setDataTag(dataTag);
        requestData.setNodeFilter(nodeFilter);
        requestData.setImportId(importId);
        return requestData;
    }

    static NodeUnregister newNodeUnregister() {
        NodeUnregister nodeUnregister = new NodeUnregister();
        nodeUnregister.setId(UUID.randomUUID().toString());
        nodeUnregister.setControlSignalType(ControlSignalType.UNREGISTER);
        return nodeUnregister;
    }

    static SupplyData newSupplyData(DataTag dataTag) {
        SupplyData supplyData = new SupplyData();
        supplyData.setId(UUID.randomUUID().toString());
        supplyData.setDataTag(dataTag);
        return supplyData;
    }

    static StopSupplyData newStopSupplyData(DataTag dataTag, Set<String> exportsId) {
        StopSupplyData stopSupplyData = new StopSupplyData();
        stopSupplyData.setDataTag(dataTag);
        stopSupplyData.setId(UUID.randomUUID().toString());
        stopSupplyData.setDataExportsId(exportsId);
        return stopSupplyData;
    }

    static ReleaseDataChannel newReleaseDataChannel(Set<String> importsId) {
        ReleaseDataChannel releaseDataChannel = new ReleaseDataChannel();
        releaseDataChannel.setId(UUID.randomUUID().toString());
        releaseDataChannel.setDataImportsId(importsId);
        return releaseDataChannel;
    }

    static RegisterJob newJobRegister(JobConfiguration jobConfiguration) {
        RegisterJob registerJob = new RegisterJob();
        registerJob.setId(UUID.randomUUID().toString());
        registerJob.setControlSignalType(ControlSignalType.REGISTER_JOB);
        registerJob.setJobConfiguration(jobConfiguration);
        return registerJob;
    }

    static RequestJob newRequestJob(String jobName) {
        RequestJob requestJob = new RequestJob();
        requestJob.setId(UUID.randomUUID().toString());
        requestJob.setJobName(jobName);
        requestJob.setControlSignalType(ControlSignalType.REQUEST_JOB);
        return requestJob;
    }
}
