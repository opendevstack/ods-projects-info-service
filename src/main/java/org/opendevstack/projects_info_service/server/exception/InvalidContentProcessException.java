package org.opendevstack.projects_info_service.server.exception;

public class InvalidContentProcessException extends RuntimeException {
    public InvalidContentProcessException(String message) {
        super(message);
    }

    public InvalidContentProcessException(String message, Exception cause) {
        super(message, cause);
    }
}
