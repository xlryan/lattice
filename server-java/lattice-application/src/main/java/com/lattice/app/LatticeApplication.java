package com.lattice.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Monolith 入口，扫描所有模块包路径。
 */
@SpringBootApplication(scanBasePackages = "com.lattice")
@ConfigurationPropertiesScan(basePackages = "com.lattice")
public class LatticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(LatticeApplication.class, args);
    }
}
