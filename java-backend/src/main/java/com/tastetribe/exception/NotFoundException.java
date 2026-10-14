package com.tastetribe.exception;

/** Thrown when a resource does not exist — mapped to HTTP 404. */
public class NotFoundException extends AppException {

    public NotFoundException(String message) {
        super(404, message);
    }
}
