package com.lattice.core.infrastructure.exception;

/**
 * Signals failures when invoking downstream services (Python worker, Firefly, etc.).
 */
public class ExternalServiceException extends LatticeException {

    public ExternalServiceException(String message, Throwable cause) {
        super(ErrorCode.EXTERNAL_SERVICE_ERROR, message, cause);
    }

    public ExternalServiceException(ErrorCode code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
