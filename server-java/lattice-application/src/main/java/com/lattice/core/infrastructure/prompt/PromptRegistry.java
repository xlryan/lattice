package com.lattice.core.infrastructure.prompt;

import org.springframework.util.Assert;

import java.util.Map;

/**
 * Runtime prompt catalogue for Spring AI interactions.
 */
public class PromptRegistry {

    private final Map<String, PromptCatalogProperties.PromptTemplateProperties> templates;

    public PromptRegistry(PromptCatalogProperties properties) {
        this.templates = Map.copyOf(properties.getTemplates());
    }

    public String resolveContent(String key) {
        var template = templates.get(key);
        Assert.notNull(template, () -> "Prompt template not configured for key=" + key);
        return template.content();
    }

    public Map<String, PromptCatalogProperties.PromptTemplateProperties> templates() {
        return templates;
    }
}
