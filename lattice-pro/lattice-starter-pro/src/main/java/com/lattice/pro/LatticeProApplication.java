package com.lattice.pro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the commercial edition of Lattice.
 */
@SpringBootApplication(scanBasePackages = {"com.lattice.core", "com.lattice.pro"})
public class LatticeProApplication {

    public static void main(String[] args) {
        SpringApplication.run(LatticeProApplication.class, args);
    }
}
