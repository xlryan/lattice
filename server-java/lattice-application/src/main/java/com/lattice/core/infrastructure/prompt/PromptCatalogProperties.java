package com.lattice.core.infrastructure.prompt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Binds prompt templates from configuration to allow runtime customization.
 */
@ConfigurationProperties(prefix = "lattice.prompts")
public class PromptCatalogProperties {

    private Map<String, PromptTemplateProperties> templates = new HashMap<>();

    public Map<String, PromptTemplateProperties> getTemplates() {
        return templates;
    }

    public void setTemplates(Map<String, PromptTemplateProperties> templates) {
        this.templates = templates;
    }

    public record PromptTemplateProperties(String description, String content) {
    }
}
