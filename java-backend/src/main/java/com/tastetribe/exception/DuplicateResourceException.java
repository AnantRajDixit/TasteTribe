package com.tastetribe.exception;

/** Thrown on duplicate username / email / category — mapped to HTTP 409. */
public class DuplicateResourceException extends AppException {

    public DuplicateResourceException(String message) {
        super(409, message);
    }
}
