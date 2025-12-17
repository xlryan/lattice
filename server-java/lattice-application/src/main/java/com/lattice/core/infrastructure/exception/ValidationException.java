package com.lattice.core.infrastructure.exception;

/**
 * Raised when inbound data fails domain validation.
 */
public class ValidationException extends LatticeException {

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_FAILURE, message);
    }
}
