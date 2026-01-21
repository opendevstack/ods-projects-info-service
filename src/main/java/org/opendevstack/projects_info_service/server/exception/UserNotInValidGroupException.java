package org.opendevstack.projects_info_service.server.exception;

public class UserNotInValidGroupException extends RuntimeException {

    public UserNotInValidGroupException(String message) {
        super(message);
    }
}
