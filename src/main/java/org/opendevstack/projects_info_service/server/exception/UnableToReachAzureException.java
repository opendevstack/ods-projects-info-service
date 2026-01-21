package org.opendevstack.projects_info_service.server.exception;

public class UnableToReachAzureException extends RuntimeException {
    public UnableToReachAzureException(String message, Throwable cause) {
        super(message, cause);
    }
}
