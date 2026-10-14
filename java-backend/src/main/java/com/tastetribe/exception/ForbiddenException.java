package com.tastetribe.exception;

/** Thrown when an authenticated user lacks permission (owner/admin checks) — HTTP 403. */
public class ForbiddenException extends AppException {

    public ForbiddenException(String message) {
        super(403, message);
    }
}
