package com.tastetribe.exception;

/** Thrown when credentials are missing or wrong — mapped to HTTP 401. */
public class UnauthorizedException extends AppException {

    public UnauthorizedException(String message) {
        super(401, message);
    }
}
