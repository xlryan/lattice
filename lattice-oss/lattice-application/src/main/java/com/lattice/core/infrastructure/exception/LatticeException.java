package com.lattice.core.infrastructure.exception;

/**
 * Base runtime exception enriched with an {@link ErrorCode} to support uniform handling.
 */
public class LatticeException extends RuntimeException {

    private final ErrorCode errorCode;

    public LatticeException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public LatticeException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
