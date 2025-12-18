package com.lattice.core.infrastructure.exception;

/**
 * Thrown when a requested domain entity cannot be located.
 */
public class ResourceNotFoundException extends LatticeException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
