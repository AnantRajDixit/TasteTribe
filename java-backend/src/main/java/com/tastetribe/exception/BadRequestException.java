package com.tastetribe.exception;

/** Thrown on invalid input that bean validation cannot express — mapped to HTTP 400. */
public class BadRequestException extends AppException {

    public BadRequestException(String message) {
        super(400, message);
    }
}
