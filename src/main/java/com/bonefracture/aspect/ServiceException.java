package com.bonefracture.aspect;

public class ServiceException extends RuntimeException {

    private final String serviceOperation;

    public ServiceException(String serviceOperation, String message, Throwable cause) {
        super(message, cause);
        this.serviceOperation = serviceOperation;
    }

    public String getServiceOperation() {
        return serviceOperation;
    }
}
