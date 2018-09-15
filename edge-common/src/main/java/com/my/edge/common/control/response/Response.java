package com.my.edge.common.control.response;

import com.my.edge.common.control.ControlSignal;
import com.my.edge.common.control.ControlSignalType;

import java.util.UUID;

public interface Response extends ControlSignal {

    String getCommandId();

    static Response newRegisterResponse(String commandId) {
        NodeRegisterResponse response = new NodeRegisterResponse();
        response.setId(UUID.randomUUID().toString());
        response.setControlSignalType(ControlSignalType.REGISTER);
        response.setCommandId(commandId);
        return response;
    }

    static Response newNodeUnregisterResponse(String commandId) {
        NodeUnregisterResponse response = new NodeUnregisterResponse();
        response.setId(UUID.randomUUID().toString());
        response.setControlSignalType(ControlSignalType.UNREGISTER);
        response.setCommandId(commandId);
        return response;
    }

    static Response newSupplyDataResponse(String commandId) {
        SupplyDataResponse response = new SupplyDataResponse();
        response.setId(UUID.randomUUID().toString());
        response.setControlSignalType(ControlSignalType.SUPPLY_DATA);
        response.setCommandId(commandId);
        return response;
    }

    static Response newStopSupplyDataResponse(String commandId) {
        StopSupplyDataResponse response = new StopSupplyDataResponse();
        response.setId(UUID.randomUUID().toString());
        response.setControlSignalType(ControlSignalType.STOP_SUPPLY_DATA);
        response.setCommandId(commandId);
        return response;
    }

    static Response newReleaseDataChannelResponse(String commandId) {
        ReleaseDataChannelResponse response = new ReleaseDataChannelResponse();
        response.setId(UUID.randomUUID().toString());
        response.setControlSignalType(ControlSignalType.RELEASE_DATA_CHANNEL);
        response.setCommandId(commandId);
        return response;
    }

    static Response newRequestDataResponse(String commandId, boolean hasData, String dataExportId) {
        RequestDataResponse requestDataResponse = new RequestDataResponse();
        requestDataResponse.setId(UUID.randomUUID().toString());
        requestDataResponse.setControlSignalType(ControlSignalType.REQUEST_DATA);
        requestDataResponse.setCommandId(commandId);
        requestDataResponse.setHasData(hasData);
        requestDataResponse.setDataExportId(dataExportId);
        return requestDataResponse;
    }

    static Response newRegisterJobResponse(String commandId, boolean succeeded, String failureReason) {
        RegisterJobResponse registerJobResponse = new RegisterJobResponse();
        registerJobResponse.setId(UUID.randomUUID().toString());
        registerJobResponse.setControlSignalType(ControlSignalType.REGISTER_JOB);
        registerJobResponse.setCommandId(commandId);
        registerJobResponse.setSucceeded(succeeded);
        registerJobResponse.setFailureReason(failureReason);
        return registerJobResponse;
    }
}
