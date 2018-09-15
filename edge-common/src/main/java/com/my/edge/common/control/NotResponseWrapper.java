package com.my.edge.common.control;

import com.my.edge.common.control.command.Command;

import static com.my.edge.common.control.NotResponseWrapper.ResponseStatus.NOT_RESPONSE;

public class NotResponseWrapper {
    public enum ResponseStatus {
        NOT_RESPONSE, SUCCEEDED_ONCE, FAILED;
    }

    private Command command;
    private int referenceCount;
    private ResponseStatus responseStatus;

    public NotResponseWrapper() {

    }

    public NotResponseWrapper(Command command, int referenceCount, ResponseStatus responseStatus) {
        this.command = command;
        this.referenceCount = referenceCount;
        this.responseStatus = responseStatus;
    }

    public NotResponseWrapper(Command command) {
        this.command = command;
        this.referenceCount = 0;
        this.responseStatus = NOT_RESPONSE;
    }

    public Command getCommand() {
        return command;
    }

    public int referenceCountPlusOne() {
        this.referenceCount += 1;
        return this.referenceCount;
    }

    public int referenceCountMinusOne() {
        this.referenceCount -= 1;
        return this.referenceCount;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public int getReferenceCount() {
        return referenceCount;
    }

    public void setReferenceCount(int referenceCount) {
        this.referenceCount = referenceCount;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        NotResponseWrapper another = (NotResponseWrapper)obj;
        return this.command.equals(another.command);
    }

    @Override
    public int hashCode() {
        return command.hashCode();
    }
}
