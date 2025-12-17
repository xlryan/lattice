package com.lattice.core.infrastructure.exception;

/**
 * Domain/business level violations that should surface as 4xx errors.
 */
public class BusinessException extends LatticeException {

    public BusinessException(String message) {
        super(ErrorCode.VALIDATION_FAILURE, message);
    }

    public BusinessException(ErrorCode code, String message) {
        super(code, message);
    }
}
