package com.tastetribe.exception;

/**
 * Base class of every custom application exception.
 *
 * <p>Demonstrates inheritance: all specialised exceptions extend this class, and the
 * single {@link GlobalExceptionHandler} handler for {@code AppException} covers the
 * whole hierarchy (polymorphic dispatch).</p>
 */
public class AppException extends RuntimeException {

    private final int status;

    public AppException(String message) {
        this(400, message);
    }

    public AppException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
