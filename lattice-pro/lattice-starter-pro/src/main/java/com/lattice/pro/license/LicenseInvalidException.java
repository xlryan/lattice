package com.lattice.pro.license;

/**
 * Raised when a provided license key fails validation.
 */
public class LicenseInvalidException extends RuntimeException {

    public LicenseInvalidException(String message) {
        super(message);
    }
}
