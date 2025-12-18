package com.lattice.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Lattice Core monolith entry point.
 */
@SpringBootApplication(scanBasePackages = "com.lattice")
@ConfigurationPropertiesScan(basePackages = "com.lattice")
public class LatticeCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(LatticeCoreApplication.class, args);
    }
}
