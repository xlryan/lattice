package com.lattice.pro.license.aop;

import com.lattice.pro.license.LicenseManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * AOP advice that ensures a valid license whenever a PRO feature is invoked.
 */
@Aspect
@Component
public class LicenseCheckAspect {

    private final LicenseManager licenseManager;
    private final Environment environment;

    public LicenseCheckAspect(LicenseManager licenseManager, Environment environment) {
        this.licenseManager = licenseManager;
        this.environment = environment;
    }

    @Before("@annotation(com.lattice.pro.license.aop.ProFeature)")
    public void checkLicense() {
        String key = environment.getProperty("lattice.license.key");
        licenseManager.verifyLicense(key);
    }
}
