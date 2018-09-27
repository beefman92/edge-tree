package com.my.edge.server;

import com.my.edge.common.control.*;
import com.my.edge.common.control.command.*;
import com.my.edge.common.control.response.*;
import com.my.edge.common.data.Data;
import com.my.edge.common.data.DataTag;
import com.my.edge.common.data.DataWrapper;
import com.my.edge.common.entity.Tuple2;
import com.my.edge.common.job.JobConfiguration;
import com.my.edge.common.network.NetworkManager;
import com.my.edge.server.config.NetworkTopology;
import com.my.edge.server.config.RequiredDataConfig;
import com.my.edge.server.control.SignalCollections;
import com.my.edge.server.data.*;
import com.my.edge.server.job.JobHandler;
import com.my.edge.server.job.JobRepository;
import com.my.edge.server.job.Transmitter;
import com.my.edge.server.util.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.SocketAddress;
import java.util.*;

public class ServerHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /*
    记录当前节点需求的数据类型。 Demo-1
     */
    private Set<DataTag> requiredDataTags = new HashSet<>();

    /*
    generatedData记录当前节点所生产的数据的数据类型。子节点生成的数据不在这里记录。
     */
    private Set<DataTag> generatedData = new HashSet<>();

    private SignalCollections signalCollections = new SignalCollections();
    private DataChannels dataChannels = new DataChannels();
    private JobRepository jobRepository = new JobRepository();

    private boolean isTop;
    private Thread commandHandler;
    private Thread dataHandler;
    private NetworkTopology networkTopology;
    private NetworkManager networkManager;
    private NodeManager nodeManager;
    private JobHandler jobHandler;

    // 当前节点的nodeMetadata应该表示的是所有子节点的NodeMetadata之并
    private NodeMetadata nodeMetadata;

    public ServerHandler() {

    }

    public void setJobHandler(JobHandler jobHandler) {
        this.jobHandler = jobHandler;
    }

    public ServerHandler(boolean isTop) {
        this.isTop = isTop;
    }

    public ServerHandler(NetworkTopology networkTopology, NetworkManager networkManager, boolean isTop) {
        this.networkTopology = networkTopology;
        this.networkManager = networkManager;
        this.isTop = isTop;
    }

    public void initialize() {
        Map<String, String> map = new HashMap<>();
        Iterator<String> temp_ = map.keySet().iterator();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("node-metadata.json")) {
            nodeMetadata = JsonSerializer.objectMapper.readValue(inputStream, NodeMetadata.class);
        } catch (Exception e) {
            throw new RuntimeException("Initializing DataHandler failed. ", e);
        }
        registerToParents(networkTopology.getParentsAddress());
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("required-data.json")) {
            RequiredDataConfig requiredDataConfig = JsonSerializer.objectMapper.readValue(inputStream, RequiredDataConfig.class);
            this.requiredDataTags = requiredDataConfig.getRequiredDataTags();
            if (logger.isDebugEnabled()) {
                for (DataTag requiredDataType: this.requiredDataTags) {
                    logger.debug("Required data type is " + requiredDataType);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Initializing DataHandler failed. ", e);
        }
        commandHandler = new Thread(() -> {
            for (DataTag generatedDataType: generatedData) {
                Iterator<Map.Entry<SocketAddress, NodeMetadata>> parents = networkTopology.getParents();
                while (parents.hasNext()) {
                    Map.Entry<SocketAddress, NodeMetadata> parent = parents.next();
                    informSuppliedData(parent.getKey(), generatedDataType);
                }
            }
            for (DataTag requiredDataTag: requiredDataTags) {
                Iterator<Map.Entry<SocketAddress, NodeMetadata>> parents = networkTopology.getParents();
                while (parents.hasNext()) {
                    Map.Entry<SocketAddress, NodeMetadata> parent = parents.next();
                    requestData(parent.getKey(), null, requiredDataTag, null);
                }
            }
            while (true) {
                try {
                    Tuple2<SocketAddress, ControlSignal> entry = networkManager.fetchControlSignal();
                    SocketAddress requester = entry.getValue1();
                    ControlSignal controlSignal = entry.getValue2();
                    if (controlSignal instanceof Command) {
                        Command command = (Command) controlSignal;
                        handleCommand(requester, command);
                    } else if (controlSignal instanceof Response) {
                        Response response = (Response) controlSignal;
                        handleResponse(requester, response);
                    } else {
                        throw new RuntimeException("Unrecognized ControlSignal " + controlSignal + " from " + requester);
                    }
                } catch (Exception e) {
                    logger.warn("Processing command from remote invocation failed. ", e);
                }
            }
        }, "command-handler");

        dataHandler = new Thread(() -> {
            while (true) {
                try {
                    Tuple2<SocketAddress, DataWrapper> entry = networkManager.fetchDataWrapper();
                    SocketAddress from = entry.getValue1();
                    DataWrapper dataWrapper = entry.getValue2();
                    Data data = dataWrapper.getData();
                    List<String> dataImportsId = dataWrapper.getDataExportsId();
                    Map<SocketAddress, Set<DataExport>> dataExports = new HashMap<>();
                    List<Transmitter> consuming = new ArrayList<>();
                    if (!dataImportsId.isEmpty()) { // 从其他节点发来的数据
                        for (String dataImportId: dataImportsId) {
                            // 转发至其他节点
                            DataExport dataExport = dataChannels.getDataExportByDataImportId(dataImportId);
                            if (dataExport != null) {
                                Set<DataExport> part = dataExports.computeIfAbsent(dataExport.getTarget(), (key) -> {
                                    return new HashSet<>();
                                });
                                part.add(dataExport);
                            }

                            // 当前节点消费
                            List<Transmitter> temp = dataChannels.getTransmitters(dataImportId);
                            if (temp != null) {
                                consuming.addAll(temp);
                            }
                        }
                    } else { // 当前节点生成的数据
                        List<DataExport> temp = dataChannels.getGeneratedDataTrasmission(data.getDataTag());
                        if (temp != null) {
                            for (DataExport dataExport: temp) {
                                Set<DataExport> part = dataExports.computeIfAbsent(dataExport.getTarget(), (key) -> {
                                    return new HashSet<>();
                                });
                                part.add(dataExport);
                            }
                        }
                    }

                    for (Transmitter transmitter: consuming) {
                        transmitter.addNewData(data);
                    }

                    for (Map.Entry<SocketAddress, Set<DataExport>> keyValue: dataExports.entrySet()) {
                        SocketAddress target = keyValue.getKey();
                        Set<DataExport> value = keyValue.getValue();
                        if (!target.equals(from)) {
                            DataWrapper newDataWrapper = new DataWrapper();
                            newDataWrapper.setData(data);
                            List<String> exportsId = new ArrayList<>();
                            for (DataExport dataExport: value) {
                                exportsId.add(dataExport.getId());
                            }
                            newDataWrapper.setDataExportsId(exportsId);
                            logger.debug("Transmit data " + JsonSerializer.objectMapper.writeValueAsString(newDataWrapper) + " to " + target);
                            networkManager.sendData(target, newDataWrapper);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Processing data from remote failed. ", e);
                }
            }
        }, "data-handler");
    }

    private void handleCommand(SocketAddress requester, Command command) {
        switch (command.getControlSignalType()) {
            case REGISTER: {
                handleRegister(requester, command);
                break;
            }
            case UNREGISTER: {
                handleUnregister(requester, command);
                break;
            }
            case REQUEST_DATA: {
                handleRequestData(requester, command);
                break;
            }
            case SUPPLY_DATA: {
                handleSupplyData(requester, (SupplyData)command);
                break;
            }
            case STOP_SUPPLY_DATA: {
                handleStopSupplyData(requester, (StopSupplyData)command);
                break;
            }
            case RELEASE_DATA_CHANNEL: {
                handleReleaseDataChannel(requester, (ReleaseDataChannel)command);
                break;
            }
            case STOP_REQUEST_DATA: {
                break;
            }
            case REGISTER_JOB: {
                handleRegisterJob(requester, (RegisterJob)command);
                break;
            }
            case REQUEST_JOB: {
                handleRequestJob(requester, (RequestJob)command);
                break;
            }
            case RUN_JOB: {
                handleRunJob(requester, (RunJob)command);
                break;
            }
            default:
                throw new RuntimeException("Unrecognized command type: " + command.getControlSignalType());
        }
    }

    public void handleRegister(SocketAddress requester, Command command) {
        networkTopology.addChild(requester, ((NodeRegister)command).getNodeMetadata());
        logger.info("Register remote node " + requester + " to children nodes. ");
        Response response = Response.newRegisterResponse(command.getId());
        networkManager.sendResponse(requester, response);
    }

    public void handleUnregister(SocketAddress requester, Command command) {
        signalCollections.remove(requester);
        networkManager.releaseConnection(requester);
        networkTopology.removeChild(requester);
        logger.info("Unregister remote node " + requester + " from children nodes. ");
        Response unregisterResponse = Response.newNodeUnregisterResponse(command.getId());
        networkManager.sendResponse(requester, unregisterResponse);
    }

    public void handleRequestData(SocketAddress requester, Command command) {
        /*
        这一阶段先不做通道与通道间数据共享的实现
         */
        RequestData requestData = (RequestData)command;
        DataTag dataTag = requestData.getDataTag();
        NodeFilter nodeFilter = requestData.getNodeFilter();
        logger.info("Receiving data request from node " + requester + " for data type " + dataTag);
        boolean responded = false;
        boolean definitelyNotMatched = true;
        if (nodeFilter.matches(nodeMetadata) && generatedData.contains(dataTag)) {
            String dataExportId = requestData.getImportId();
            DataExport dataExport = new DataExport(dataExportId, requester);
            dataChannels.addDataExport(dataExport);
            dataChannels.combineDataTagWithDataExport(dataTag, dataExport);
            Response response = Response.newRequestDataResponse(
                    requestData.getId(), true, dataExportId);
            networkManager.sendResponse(requester, response);
            responded = true;
            definitelyNotMatched = false;

            // demo code start
            RunJob runJob = Command.newRunJob("test-register-job");
            runJob.setConsumer(false);
            jobHandler.addRunJob(runJob);
            // demo code end
        }

        // 向别的节点进行请求
        int relation = nodeFilter.relation(this.nodeMetadata);
        switch (relation) {
            case 2:
            case -2: {
                Iterator<Map.Entry<SocketAddress, NodeMetadata>> children = networkTopology.getChildren();
                while (children.hasNext()) {
                    Map.Entry<SocketAddress, NodeMetadata> child = children.next();
                    SocketAddress childAdd = child.getKey();
                    NodeMetadata childMeta = child.getValue();
                    if (nodeFilter.matches(childMeta) && childMeta.hasDataTag(dataTag) && !requester.equals(childAdd)) {
                        definitelyNotMatched = false;
                        sendDataRequestToNext(requester, requestData, childAdd, responded);
                    }
                }
                break;
            }
            case 1:
            case -1: {
                Iterator<Map.Entry<SocketAddress, NodeMetadata>> children = networkTopology.getChildren();
                while (children.hasNext()) {
                    Map.Entry<SocketAddress, NodeMetadata> child = children.next();
                    NodeMetadata childMeta = child.getValue();
                    if (nodeFilter.matches(childMeta) && childMeta.hasDataTag(dataTag) && !requester.equals(child.getKey())) {
                        definitelyNotMatched = false;
                        sendDataRequestToNext(requester, requestData, child.getKey(), responded);
                    }
                }
                Iterator<Map.Entry<SocketAddress, NodeMetadata>> parents = networkTopology.getParents();
                while (parents.hasNext()) {
                    Map.Entry<SocketAddress, NodeMetadata> parent = parents.next();
                    if (!requester.equals(parent.getKey())) {
                        definitelyNotMatched = false;
                        sendDataRequestToNext(requester, requestData, parent.getKey(), responded);
                    }
                }
                break;
            }
            case 0: {
                Iterator<Map.Entry<SocketAddress, NodeMetadata>> parents = networkTopology.getParents();
                while (parents.hasNext()) {
                    Map.Entry<SocketAddress, NodeMetadata> parent = parents.next();
                    if (!requester.equals(parent.getKey())) {
                        definitelyNotMatched = false;
                        sendDataRequestToNext(requester, requestData, parent.getKey(), responded);
                    }
                }
                break;
            }
            default:
                throw new RuntimeException("Unsupported relation type. ");
        }

        if (definitelyNotMatched) {
            String dataExportId = requestData.getImportId();
            Response response = Response.newRequestDataResponse(
                    requestData.getId(), false, dataExportId);
            networkManager.sendResponse(requester, response);
        }
    }

    private void handleSupplyData(SocketAddress child, SupplyData supplyData) {
        DataTag dataTag = supplyData.getDataTag();
        this.nodeMetadata.addDataTag(dataTag);
        boolean firstAppearance = true;
        Iterator<Map.Entry<SocketAddress, NodeMetadata>> iterator = networkTopology.getChildren();
        while (iterator.hasNext()) {
            Map.Entry<SocketAddress, NodeMetadata> entry = iterator.next();
            NodeMetadata childMeta = entry.getValue();
            if (childMeta.hasDataTag(dataTag)) {
                firstAppearance = false;
                break;
            }
        }
        networkTopology.addChildDataTag(child, dataTag);
        logger.info("Node " + child + " starts supplying data of " + dataTag + ". ");
        Response response = Response.newSupplyDataResponse(supplyData.getId());
        networkManager.sendResponse(child, response);

        if (firstAppearance) {
            logger.info("DataTag " + JsonSerializer.writeValueAsString(dataTag) + " is new. Informing it to parents. ");
            Iterator<Map.Entry<SocketAddress, NodeMetadata>> addressIterator = networkTopology.getParents();
            while (addressIterator.hasNext()) {
                SocketAddress address = addressIterator.next().getKey();
                SupplyData supplyDataToNext = Command.newSupplyData(dataTag);
                signalCollections.addSelfPendingCommands(address, supplyDataToNext);
                networkManager.sendCommand(address, supplyDataToNext);
            }
        }
    }

    private void handleStopSupplyData(SocketAddress child, StopSupplyData stopSupplyData) {
        // 从Metadata中移除这个节点提供的数据的dataTag
        DataTag stopDataTag = stopSupplyData.getDataTag();
        networkTopology.removeChildDataTag(child, stopDataTag);
        Iterator<Map.Entry<SocketAddress, NodeMetadata>> iterator = networkTopology.getChildren();
        boolean noMoreThisTag = true;
        while (iterator.hasNext()) {
            Map.Entry<SocketAddress, NodeMetadata> entry = iterator.next();
            if (entry.getValue().hasDataTag(stopDataTag)) {
                noMoreThisTag = false;
                break;
            }
        }
        if (noMoreThisTag) {
            this.nodeMetadata.removeDataTag(stopDataTag);
        }

        // 释放相关的DataImport和DataExport，并通知相应的节点
        Set<DataExport> dataExports = dataChannels.removeChannelsByImportsId(stopSupplyData.getDataExportsId());
        Response response = Response.newStopSupplyDataResponse(stopSupplyData.getId());
        networkManager.sendResponse(child, response);

        Map<SocketAddress, Set<String>> targetToExports = new HashMap<>();
        for (DataExport dataExport: dataExports) {
            SocketAddress target = dataExport.getTarget();
            Set<String> dataExportsId = targetToExports.computeIfAbsent(target, (key) -> {
                return new HashSet<>();
            });
            dataExportsId.add(dataExport.getId());
        }
        for (Map.Entry<SocketAddress, Set<String>> entry: targetToExports.entrySet()) {
            SocketAddress target = entry.getKey();
            Set<String> exportsId = entry.getValue();
            Command commandToNext = null;
            if (networkTopology.isParent(target)) {
                if (noMoreThisTag) {
                    commandToNext = Command.newStopSupplyData(stopDataTag, exportsId);
                } else {
                    commandToNext = Command.newReleaseDataChannel(exportsId);
                }
            } else if (networkTopology.isChildren(target)) {
                commandToNext = Command.newReleaseDataChannel(exportsId);
            }
            signalCollections.addSelfPendingCommands(target, commandToNext);
            networkManager.sendCommand(target, commandToNext);
        }
    }

    private void handleReleaseDataChannel(SocketAddress requester, ReleaseDataChannel releaseDataChannel) {
        Set<DataImport> dataImports = dataChannels.removeChannelsByExportsId(releaseDataChannel.getDataImportsId());
        Response response = Response.newReleaseDataChannelResponse(releaseDataChannel.getId());
        networkManager.sendResponse(requester, response);

        Map<SocketAddress, Set<String>> sourceToImports = new HashMap<>();
        for (DataImport dataImport: dataImports) {
            SocketAddress source = dataImport.getSource();
            Set<String> dataImportsId = sourceToImports.computeIfAbsent(source, (key) -> {
                return new HashSet<>();
            });
            dataImportsId.add(dataImport.getId());
        }
        for (Map.Entry<SocketAddress, Set<String>> entry: sourceToImports.entrySet()) {
            SocketAddress target = entry.getKey();
            Set<String> importsId = entry.getValue();
            ReleaseDataChannel commandToNext = Command.newReleaseDataChannel(importsId);
            signalCollections.addSelfPendingCommands(target, commandToNext);
            networkManager.sendCommand(target, commandToNext);
        }
    }

    /**
     * 向target转发请求数据的消息
     * 在转发消息之前会先建立DataChannel。其中DataExport的id与request中DataImport的id相同，DataImport的id则是新生成的。
     * 对于同一个request，可能会向多个节点转发请求，因而可能会多次调用这个方法。
     * @param requester request的来源
     * @param request 请求
     * @param target 转发的目标
     */
    private void sendDataRequestToNext(SocketAddress requester, RequestData request, SocketAddress target, boolean responded) {
        // 建立channel
        String dataExportId = request.getImportId();
        String importId = UUID.randomUUID().toString();
        DataImport dataImport = new DataImport(importId, target);
        if (dataChannels.hasDataExport(dataExportId)) {
            // 如果对应的DataExport已经建立，只需要将新的DataImport与之关联即可
            dataChannels.addChannel(dataImport, dataExportId);
        } else {
            // 建立新的DataExport，并与DataImport进行关联
            DataExport dataExport = new DataExport(dataExportId, requester);
            dataChannels.addChannel(dataImport, dataExport);
        }

        // 向其他节点发送请求
        RequestData requestToNext = Command.newRequestDataCommand(request.getDataTag(),
                request.getNodeFilter(), importId);
        if (signalCollections.containsNotResponseCommand(request)) { // 已经有其他的self-pending请求与当前request相关联
            signalCollections.addSelfPendingCommands(target, requestToNext);
            signalCollections.relate(request, requestToNext);
        } else {
            signalCollections.addCommands(request, requester, requestToNext, target);
        }
        if (responded) {
            signalCollections.setNotResponseCommandStatus(request.getId(), NotResponseWrapper.ResponseStatus.SUCCEEDED_ONCE);
        }
        networkManager.sendCommand(target, requestToNext);
    }

    private void handleRegisterJob(SocketAddress requester, RegisterJob registerJob) {
        jobRepository.addJobConfiguration(registerJob.getJobConfiguration());
        JobDataTag jobDataTag = new JobDataTag();
        jobDataTag.setJobName(registerJob.getJobConfiguration().getJobName());
        this.nodeMetadata.addDataTag(jobDataTag);
        // TODO: 向哪些节点通知任务注册成功的消息呢？
        logger.info("Register job " + registerJob.getJobConfiguration().getJobName() + " in current node. ");
        Response response = Response.newRegisterJobResponse(registerJob.getId(), true, "");
        networkManager.sendResponseAndClose(requester, response);
    }

    private void handleRequestJob(SocketAddress requester, RequestJob requestJob) {
        String jobName = requestJob.getJobName();
        JobConfiguration jobConfiguration = jobRepository.getJobConfiguration(jobName);
        if (jobConfiguration != null) { // 当前节点有job
            Response response = Response.newRequestJobResponse(requestJob.getId(), jobConfiguration);
            networkManager.sendResponse(requester, response);
        } else { // 向其他节点转发请求
            signalCollections.addNotResponseCommand(requester, requestJob);
            Iterator<SocketAddress> iterator = networkTopology.getParentsAddress();
            while (iterator.hasNext()) {
                SocketAddress parent = iterator.next();
                RequestJob newRequest = Command.newRequestJob(requestJob.getJobName());
                networkManager.sendCommand(parent, newRequest);
                signalCollections.addSelfPendingCommands(parent, newRequest);
                signalCollections.relate(requestJob, newRequest);
            }
        }
    }

    private void handleRunJob(SocketAddress socketAddress, RunJob runJob) {
        String jobName = runJob.getJobName();
        nodeManager.addRunJob(runJob);
        Response response = Response.newRunJobResponse(runJob.getId(), runJob.getJobName(), true, null);
        networkManager.sendResponse(socketAddress, response);
    }

    private void stopRequestData(SocketAddress requester, Command command) {

    }

    private void handleResponse(SocketAddress from, Response response) {
        switch (response.getControlSignalType()) {
            case REGISTER: {
                handleNodeRegisterResponse(from, (NodeRegisterResponse) response);
                break;
            }
            case UNREGISTER: {
                handleNodeUnregisterResponse(from, (NodeUnregisterResponse) response);
                break;
            }
            case SUPPLY_DATA: {
                handleSupplyDataResponse(from, (SupplyDataResponse) response);
                break;
            }
            case STOP_SUPPLY_DATA: {
                handleStopSupplyDataResponse(from, (StopSupplyDataResponse) response);
                break;
            }
            case RELEASE_DATA_CHANNEL: {
                handleReleaseDataResponse(from, (ReleaseDataChannelResponse) response);
                break;
            }
            case REQUEST_DATA: {
                handleRequestDataResponse(from, (RequestDataResponse) response);
                break;
            }
            case REQUEST_JOB: {
                handleRequestJobResponse(from, (RequestJobResponse) response);
                break;
            }
        }
    }

    private void handleNodeRegisterResponse(SocketAddress from, NodeRegisterResponse nodeRegisterResponse) {
        String commandId = nodeRegisterResponse.getCommandId();
        logger.info("Received response of NodeRegister for " + commandId + " from " + from + ". ");
        signalCollections.removeSelfPendingCommand(commandId);
    }

    private void handleNodeUnregisterResponse(SocketAddress from, NodeUnregisterResponse nodeUnregisterResponse) {
        String commandId = nodeUnregisterResponse.getCommandId();
        logger.info("Received response of NodeUnregister for " + commandId + " from " + from + ". ");
        signalCollections.removeSelfPendingCommand(commandId);
    }

    private void handleSupplyDataResponse(SocketAddress from, SupplyDataResponse supplyDataResponse) {
        String commandId = supplyDataResponse.getCommandId();
        logger.info("Received response of SupplyData for " + commandId + " from " + from + ". ");
        signalCollections.removeSelfPendingCommand(commandId);
    }

    private void handleReleaseDataResponse(SocketAddress from, ReleaseDataChannelResponse releaseDataChannelResponse) {
        String commandId = releaseDataChannelResponse.getCommandId();
        logger.info("Received response of ReleaseDataChannel for " + commandId + " from " + from + ". ");
        signalCollections.removeSelfPendingCommand(commandId);
    }

    private void handleStopSupplyDataResponse(SocketAddress from, StopSupplyDataResponse stopSupplyDataResponse) {
        String commandId = stopSupplyDataResponse.getCommandId();
        logger.info("Received response of StopSupplyData for " + commandId + " from " + from + ". ");
        signalCollections.removeSelfPendingCommand(commandId);
    }

    private void handleRequestDataResponse(SocketAddress from, RequestDataResponse requestDataResponse) {
        // TODO: 删除SelfPending和NotResponse时，应该成对删除
        String commandId = requestDataResponse.getCommandId();
        logger.info("Received response of RequestData for " + commandId + " from " + from + ". ");
        RequestData selfPending = (RequestData) signalCollections.getSelfPendingCommand(commandId);
        Tuple2<SocketAddress, Command> notResponse = signalCollections.getNotResponseBySentCommandId(commandId);
        DataTag requestedDataTag = selfPending.getDataTag();
        if (notResponse != null) { // 中继节点
            NotResponseWrapper.ResponseStatus commandStatus = signalCollections.getNotResponseCommandStatus(
                    notResponse.getValue2().getId());
            if (requestDataResponse.isHasData()) {
                // 远端请求到了数据
                if (commandStatus == NotResponseWrapper.ResponseStatus.SUCCEEDED_ONCE) {
                    // 之前已经向下游节点通报了请求数据成功的消息，因此不再进行通报，只需删除相应的请求即可
                    signalCollections.removeCommands(notResponse.getValue2(), notResponse.getValue1(), selfPending, from);
                    logger.info("Since current node has found request data " + requestedDataTag +
                            " for " + notResponse.getValue1() + " and informed it, ignoring this success response. ");
                } else {
                    // 删除相应的请求，设置返回状态，进行通报
                    int count = signalCollections.removeCommands(notResponse.getValue2(), notResponse.getValue1(), selfPending, from);
                    if (count > 0) {
                        signalCollections.setNotResponseCommandStatus(notResponse.getValue2().getId(),
                                NotResponseWrapper.ResponseStatus.SUCCEEDED_ONCE);
                    }
                    String dataImportId = requestDataResponse.getDataExportId();
                    DataExport dataExport = dataChannels.getDataExportByDataImportId(dataImportId);
                    Response response = Response.newRequestDataResponse(notResponse.getValue2().getId(),
                            requestDataResponse.isHasData(), dataExport.getId());
                    logger.info("Current node find request data " + requestedDataTag + " for " + notResponse.getValue1() +
                            ". Sending success response to it. ");
                    networkManager.sendResponse(notResponse.getValue1(), response);
                }
            } else {
                // 未请求到数据
                if (commandStatus == NotResponseWrapper.ResponseStatus.SUCCEEDED_ONCE) {
                    // 不向下游节点进行通报，删除相应的请求和数据通道
                    signalCollections.removeCommands(notResponse.getValue2(), notResponse.getValue1(), selfPending, from);
                    String dataImportId = requestDataResponse.getDataExportId();
                    dataChannels.removeChannelByImportId(dataImportId);
                    logger.info("Since current node has found request data " + requestedDataTag +
                            " for " + notResponse.getValue1() + " and informed it, ignoring this failure response. ");
                } else {
                    // 删除相应的请求和数据通道。如果删除请求后not-response command的引用数为零，则向下游通报请求失败的消息
                    int count = signalCollections.removeCommands(notResponse.getValue2(), notResponse.getValue1(), selfPending, from);
                    String dataImportId = requestDataResponse.getDataExportId();
                    DataExport dataExport = dataChannels.removeChannelByImportId(dataImportId);
                    if (count == 0) {
                        Response response = Response.newRequestDataResponse(notResponse.getValue2().getId(),
                                requestDataResponse.isHasData(), dataExport.getId());
                        logger.info("Since all relayed request for data " + selfPending.getDataTag() + " failed, " +
                                "sending failure response to node " + notResponse.getValue1());
                        networkManager.sendResponse(notResponse.getValue1(), response);
                    } else {
                        signalCollections.setNotResponseCommandStatus(notResponse.getValue2().getId(),
                                NotResponseWrapper.ResponseStatus.FAILED);
                        logger.info("Ignoring this failure response for data " + requestedDataTag +
                                ", and waiting for other response. ");
                    }
                }
            }

        } else { // 请求数据的节点
            signalCollections.removeSelfPendingCommand(commandId);
            if (requestDataResponse.isHasData()) {
                // TODO: 在DataChannels中将对应的Transmitter标记为有效的
            } else {
                // TODO: 移除对应的DataImport和Transmitter
            }
        }
    }

    private void handleRequestJobResponse(SocketAddress from, RequestJobResponse requestJobResponse) {
        String commandId = requestJobResponse.getCommandId();
        logger.info("Received response of RequestJob for " + commandId + " from " + from + ". ");
        RequestJob selfPending = (RequestJob) signalCollections.getSelfPendingCommand(commandId);
        Tuple2<SocketAddress, Command> notResponse = signalCollections.getNotResponseBySentCommandId(commandId);
        if (notResponse != null) {
            String jobName = ((RequestJob)notResponse.getValue2()).getJobName();
            NotResponseWrapper.ResponseStatus commandStatus = signalCollections.getNotResponseCommandStatus(
                    notResponse.getValue2().getId());
            if (requestJobResponse.isHasJob()) {
                // 远端请求到了数据
                if (commandStatus == NotResponseWrapper.ResponseStatus.SUCCEEDED_ONCE) {
                    // 之前已经向下游节点通报了请求数据成功的消息，因此不再进行通报，只需删除相应的请求即可
                    signalCollections.removeCommands(notResponse.getValue2(), notResponse.getValue1(), selfPending, from);
                    logger.info("Since current node has found request job " + jobName +
                            " for " + notResponse.getValue1() + " and informed it, ignoring this success response. ");
                } else {
                    // 删除相应的请求，设置返回状态，进行通报
                    int count = signalCollections.removeCommands(notResponse.getValue2(), notResponse.getValue1(), selfPending, from);
                    if (count > 0) {
                        signalCollections.setNotResponseCommandStatus(notResponse.getValue2().getId(),
                                NotResponseWrapper.ResponseStatus.SUCCEEDED_ONCE);
                    }
                    logger.info("Current node find request job " + jobName + " for " + notResponse.getValue1() +
                            ". Sending success response to it. ");
                    Response response = Response.newRequestJobResponse(notResponse.getValue2().getId(),
                            requestJobResponse.getJobConfiguration());
                    networkManager.sendResponse(notResponse.getValue1(), response);
                }
            } else {
                // 未请求到数据
                if (commandStatus == NotResponseWrapper.ResponseStatus.SUCCEEDED_ONCE) {
                    // 不向下游节点进行通报，删除相应的请求
                    signalCollections.removeCommands(notResponse.getValue2(), notResponse.getValue1(), selfPending, from);
                    logger.info("Since current node has found request job " + jobName +
                            " for " + notResponse.getValue1() + " and informed it, ignoring this failure response. ");
                } else {
                    // 删除相应的请求。如果删除请求后not-response command的引用数为零，则向下游通报请求失败的消息
                    int count = signalCollections.removeCommands(notResponse.getValue2(), notResponse.getValue1(), selfPending, from);
                    if (count == 0) {
                        Response response = Response.newRequestJobResponse(notResponse.getValue2().getId(),
                                null);
                        logger.info("Since all relayed request for job " + jobName + " failed, " +
                                "sending failure response to node " + notResponse.getValue1());
                        networkManager.sendResponse(notResponse.getValue1(), response);
                    } else {
                        signalCollections.setNotResponseCommandStatus(notResponse.getValue2().getId(),
                                NotResponseWrapper.ResponseStatus.FAILED);
                        logger.info("Ignoring this failure response for request job " + jobName +
                                ", and waiting for other response. ");
                    }
                }
            }
        } else {
            signalCollections.removeSelfPendingCommand(requestJobResponse.getCommandId());
            signalCollections.addRequestJobResponse(requestJobResponse.getCommandId(), requestJobResponse);
        }
    }

    public void setNetworkTopology(NetworkTopology networkTopology) {
        this.networkTopology = networkTopology;
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public void setNodeManager(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    public void setIsTop(boolean isTop) {
        this.isTop = isTop;
    }

    public void addGenerateData(DataTag dataTag) {
        this.generatedData.add(dataTag);
    }

    public void addData(SocketAddress invoker, Data data) {
        DataWrapper wrapper = new DataWrapper();
        wrapper.setData(data);
        wrapper.setDataExportsId(new ArrayList<>());
        networkManager.addDataWrapper(invoker, wrapper);
    }

    public void informSuppliedData(SocketAddress remote, DataTag dataTag) {
        SupplyData supplyData = Command.newSupplyData(dataTag);
        signalCollections.addSelfPendingCommands(remote, supplyData);
        networkManager.sendCommand(remote, supplyData);
    }

    public void requestData(SocketAddress remote, NodeFilter nodeFilter, DataTag dataTag, Transmitter transmitter) {
        DataImport dataImport = new DataImport(UUID.randomUUID().toString(), remote);
        dataChannels.addDataImport(dataImport);
        dataChannels.markConsumeData(dataImport, transmitter);
        Command request = Command.newRequestDataCommand(dataTag, nodeFilter, dataImport.getId());
        signalCollections.addSelfPendingCommands(remote, request);
        networkManager.sendCommand(remote, request);
    }

    public void requestData(NodeFilter nodeFilter, DataTag dataTag, Transmitter transmitter) {
        Iterator<SocketAddress> iterator = networkTopology.getParentsAddress();
        while (iterator.hasNext()) {
            SocketAddress parent = iterator.next();
            requestData(parent, nodeFilter, dataTag, transmitter);
        }
    }

    public void registerToParents(Iterator<SocketAddress> parents) {
        if (parents != null) {
            while (parents.hasNext()) {
                SocketAddress parent = parents.next();
                registerToParent(parent);
            }
        }
    }

    public void registerToParent(SocketAddress parent) {
        Command register = Command.newRegisterCommand(this.nodeMetadata);
        networkManager.sendCommand(parent, register);
    }


    /**
     * 根据jobName请求JobConfiguration信息，目前只会向parents请求job。
     * 如果当前节点的JobRepository中有目标Job，返回相应的JobConfiguration
     * 否则向远端节点进行请求。这是一个阻塞的方法
     * @param jobName job name
     * @return 找到job，返回JobConfiguration；否则返回null
     */
    public JobConfiguration requestJobConfiguration(String jobName, boolean cache) {
        JobConfiguration jobConfiguration = jobRepository.getJobConfiguration(jobName);
        if (jobConfiguration == null) {
            List<String> requestIds = new ArrayList<>();
            Iterator<SocketAddress> iterator = networkTopology.getParentsAddress();
            while (iterator.hasNext()) {
                RequestJob requestJob = Command.newRequestJob(jobName);
                SocketAddress parent = iterator.next();
                networkManager.sendCommand(parent, requestJob);
                signalCollections.addSelfPendingCommands(parent, requestJob);
                requestIds.add(requestJob.getId());
            }
            List<Integer> removedIndices = new ArrayList<>();
            boolean stop = false;
            while (!stop) {
                try {
                    Thread.sleep(1000L);
                    for (int i = 0; i < requestIds.size(); i++) {
                        String id = requestIds.get(i);
                        RequestJobResponse response = signalCollections.getRequestJobResponse(id);
                        if (response != null) {
                            removedIndices.add(i);
                            signalCollections.removeRequestJobResponse(id);
                            if (response.isHasJob()) {
                                jobConfiguration = response.getJobConfiguration();
                                stop = true;
                            }
                        }
                    }
                    if (stop) {
                        removedIndices.clear();
                        requestIds.clear();
                    } else {
                        for (int i = removedIndices.size() - 1; i >= 0; i--) {
                            Integer index = removedIndices.get(i);
                            requestIds.remove(index);
                        }
                        if (requestIds.isEmpty()) {
                            stop = true;
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Requesting job encounter error. ", e);
                }
            }

            if (jobConfiguration != null && cache) {
                jobRepository.addJobConfiguration(jobConfiguration);
            }
        }
        return jobConfiguration;
    }

    public void start() {
        commandHandler.start();
        dataHandler.start();
    }
}
