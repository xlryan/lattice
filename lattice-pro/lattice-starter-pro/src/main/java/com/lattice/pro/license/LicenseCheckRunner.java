package com.lattice.pro.license;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Performs license validation when the application starts.
 */
@Component
public class LicenseCheckRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LicenseCheckRunner.class);
    private final LicenseManager licenseManager;
    private final Environment environment;

    public LicenseCheckRunner(LicenseManager licenseManager, Environment environment) {
        this.licenseManager = licenseManager;
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        String key = environment.getProperty("lattice.license.key");
        try {
            licenseManager.verifyLicense(key);
            log.info("License check passed.");
        } catch (LicenseInvalidException ex) {
            log.error("License check failed: {}", ex.getMessage());
            throw ex;
        }
    }
}
