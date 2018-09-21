package com.my.edge.server.control;

import com.my.edge.common.control.NotResponseWrapper;
import com.my.edge.common.control.command.Command;
import com.my.edge.common.control.response.RequestJobResponse;
import com.my.edge.common.entity.Tuple2;
import com.my.edge.server.util.JsonSerializer;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SignalCollections {
    /*
    当前节点向其他节点发出的请求中，还没有收到回复的请求。按照所请求的节点的地址进行分类
     */
    private Map<String, Tuple2<SocketAddress, Command>> selfPendingCommands;

    /*
    来自其他节点的请求。这些请求经过了初步的处理，但因为某些原因尚不能作出回复的请求。
    按照发起请求的节点的地址进行分类
     */
    private Map<String, Tuple2<SocketAddress, NotResponseWrapper>> notResponseCommands;

    private Map<String, Set<String>> receivedToSent;
    private Map<String, Set<String>> sentToReceived;
    private ReentrantReadWriteLock lock;

    private Map<String, RequestJobResponse> requestJobResponses;

    public SignalCollections() {
        this.selfPendingCommands = new HashMap<>();
        this.notResponseCommands = new HashMap<>();
        this.receivedToSent = new HashMap<>();
        this.sentToReceived = new HashMap<>();
        this.requestJobResponses = new HashMap<>();
        lock = new ReentrantReadWriteLock();
    }

    public void remove(SocketAddress socketAddress) {
        lock.writeLock().lock();
        try {
            Set<String> removedCommandIds = new HashSet<>();
            for (Map.Entry<String, Tuple2<SocketAddress, NotResponseWrapper>> entry: notResponseCommands.entrySet()) {
                String commandId = entry.getKey();
                if (socketAddress.equals(entry.getValue().getValue1())) {
                    removedCommandIds.add(commandId);
                }
            }
            for (String removedCommandId: removedCommandIds) {
                notResponseCommands.remove(removedCommandId);
                receivedToSent.remove(removedCommandId);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 删除self-pending command。这个方法只能针对没有与not-responded command关联的command使用
     * @param commandId command id
     */
    public void removeSelfPendingCommand(String commandId) {
        lock.writeLock().lock();
        try {
            selfPendingCommands.remove(commandId);
            Set<String> correspondingReceived = sentToReceived.remove(commandId);
            if (correspondingReceived != null) {
                throw new RuntimeException("This method can only be invoked when removing self-pending command with no corresponding not-responded command. ");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

//    public void removeNotResponseCommand(String commandId) {
//        lock.writeLock().lock();
//        try {
//            notResponseCommands.remove(commandId);
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }

    /**
     * 将一个NotResponseCommand和一个SelfPendingCommand关联起来，
     * 要求在调用这个方法之前，两个command已经加入到了selfPendingCommands和notResponseCommands。
     * 因此，在调用这个方法之前，应先通过其他的方法将相应的command添加到对应的集合中
     * @param receivedCommand 从其他节点接收到的请求
     * @param sentCommand 向其他节点发出的请求
     */
    public void relate(Command receivedCommand, Command sentCommand) {
        lock.writeLock().lock();
        try {
            Set<String> sentIds = receivedToSent.computeIfAbsent(receivedCommand.getId(), (key) -> {
               return new HashSet<>();
            });
            sentIds.add(sentCommand.getId());

            Set<String> receivedIds = sentToReceived.computeIfAbsent(sentCommand.getId(), (key) -> {
                return new HashSet<>();
            });
            receivedIds.add(receivedCommand.getId());
            Tuple2<SocketAddress, NotResponseWrapper> tuple2 = notResponseCommands.get(receivedCommand.getId());
            if (tuple2 == null) {
                throw new RuntimeException("Cannot find not-responded command with id " + receivedCommand.getId());
            }
            tuple2.getValue2().referenceCountPlusOne();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Tuple2<SocketAddress, Command> getNotResponseBySentCommandId(String sendCommandId) {
        lock.readLock().lock();
        try {
            Set<String> notResponseIds = sentToReceived.get(sendCommandId);
            if (notResponseIds == null) {
                return null;
            }
            if (notResponseIds.size() > 1) {
                throw new RuntimeException("A self-pending command can only attach to one not-responded command. " +
                        "However, command " + sendCommandId + " attaches two commands " +
                        JsonSerializer.writeValueAsString(notResponseIds));
            }
            String notResponseId = notResponseIds.iterator().next();
            Tuple2<SocketAddress, NotResponseWrapper> wrapper = notResponseCommands.get(notResponseId);
            return new Tuple2<>(wrapper.getValue1(), wrapper.getValue2().getCommand());
        } finally {
            lock.readLock().unlock();
        }
    }

    public NotResponseWrapper.ResponseStatus getNotResponseCommandStatus(String notResponseId) {
        lock.readLock().lock();
        try {
            Tuple2<SocketAddress, NotResponseWrapper> wrapper = notResponseCommands.get(notResponseId);
            if (wrapper == null) {
                throw new RuntimeException("No such not-responded command with id " + notResponseId);
            }
            return wrapper.getValue2().getResponseStatus();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setNotResponseCommandStatus(String notResponseId, NotResponseWrapper.ResponseStatus responseStatus) {
        lock.writeLock().lock();
        try {
            Tuple2<SocketAddress, NotResponseWrapper> wrapper = notResponseCommands.get(notResponseId);
            if (wrapper == null) {
                throw new RuntimeException("No such not-responded command with id " + notResponseId);
            }
            wrapper.getValue2().setResponseStatus(responseStatus);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Command getSelfPendingCommand (String commandId) {
        lock.writeLock().lock();
        try {
            Tuple2<SocketAddress, Command> selfPending = selfPendingCommands.get(commandId);
            return selfPending.getValue2();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addNotResponseCommand(SocketAddress requester, Command command) {
        lock.writeLock().lock();
        try {
            NotResponseWrapper wrapper = new NotResponseWrapper(command);
            Tuple2<SocketAddress, NotResponseWrapper> tuple = new Tuple2<>(requester, wrapper);
            notResponseCommands.put(command.getId(), tuple);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addSelfPendingCommands(SocketAddress target, Command command) {
        lock.writeLock().lock();
        try {
            Tuple2<SocketAddress, Command> tuple = new Tuple2<>(target, command);
            selfPendingCommands.put(command.getId(), tuple);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addCommands(Command notResponded, SocketAddress notRespondedHost,
                            Command selfPending, SocketAddress selfPendingTarget) {
        lock.writeLock().lock();
        try {
            String notRespondedId = notResponded.getId();
            Tuple2<SocketAddress, NotResponseWrapper> tuple = notResponseCommands.get(notRespondedId);
            if (tuple == null) {
                tuple = new Tuple2<>(notRespondedHost, new NotResponseWrapper(notResponded));
                tuple.getValue2().referenceCountPlusOne();
                notResponseCommands.put(notRespondedId, tuple);
            } else {
                if (!tuple.getValue1().equals(notRespondedHost)) {
                    throw new RuntimeException("Addresses do not match");
                }
                tuple.getValue2().referenceCountPlusOne();
            }

            String selfPendingId = selfPending.getId();
            if (selfPendingCommands.containsKey(selfPendingId)) {
                throw new RuntimeException("Self-pending command has already existed. ");
            }
            Tuple2<SocketAddress, Command> newTuple = new Tuple2<>(selfPendingTarget, selfPending);
            selfPendingCommands.put(selfPendingId, newTuple);

            Set<String> sentIds = receivedToSent.computeIfAbsent(notRespondedId, (key) -> {
                return new HashSet<>();
            });
            sentIds.add(selfPendingId);

            Set<String> receivedIds = sentToReceived.computeIfAbsent(selfPendingId, (key) -> {
                return new HashSet<>();
            });
            receivedIds.add(notRespondedId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int removeCommands(Command notRespondedCommand, SocketAddress notRespondedHost,
                              Command selfPendingCommand, SocketAddress selfPendingTarget) {
        lock.writeLock().lock();
        try {
            String notRespondedId = notRespondedCommand.getId();
            Tuple2<SocketAddress, NotResponseWrapper> tuple = notResponseCommands.get(notRespondedId);
            if (!tuple.getValue1().equals(notRespondedHost)) {
                throw new RuntimeException("FUCK! ");
            }
            int referenceCount = tuple.getValue2().referenceCountMinusOne();
            if (referenceCount == 0) {
                notResponseCommands.remove(notRespondedId);
            }

            String selfPendingId = selfPendingCommand.getId();
            Tuple2<SocketAddress, Command> newTuple = selfPendingCommands.remove(selfPendingId);
            if (!newTuple.getValue1().equals(selfPendingTarget)) {
                throw new RuntimeException("FUCK! ");
            }

            Set<String> temp = sentToReceived.remove(selfPendingId);
            if (temp.size() != 1) {
                throw new RuntimeException("FUCK! ");
            }
            temp = receivedToSent.get(notRespondedId);
            temp.remove(selfPendingId);
            if (temp.isEmpty()) {
                receivedToSent.remove(notRespondedId);
            }
            return referenceCount;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean containsNotResponseCommand(Command notResponseCommand) {
        lock.readLock().lock();
        try {
            String id = notResponseCommand.getId();
            return notResponseCommands.containsKey(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    public RequestJobResponse getRequestJobResponse(String requestId) {
        return requestJobResponses.get(requestId);
    }

    public void addRequestJobResponse(String requestId, RequestJobResponse requestJobResponse) {
        this.requestJobResponses.put(requestId, requestJobResponse);
    }

    public void removeRequestJobResponse(String requestId) {
        requestJobResponses.remove(requestId);
    }
}
