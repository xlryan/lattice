package com.lattice.agent.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 模块级配置，暴露 ConfigurationProperties 供外层引用。
 */
@Configuration
@EnableConfigurationProperties({AgentAiProperties.class, FireflyProperties.class})
public class AgentModuleConfiguration {
}
