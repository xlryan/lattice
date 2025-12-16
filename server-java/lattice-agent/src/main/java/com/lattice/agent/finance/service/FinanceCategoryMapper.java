package com.lattice.agent.finance.service;

import com.lattice.agent.config.FireflyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * 负责把自然语言类别/账户映射到 Firefly 已存在的名称，确保工具调用成功。
 */
@Component
@RequiredArgsConstructor
public class FinanceCategoryMapper {

    private final FireflyProperties properties;

    public String resolveCategory(String rawCategory) {
        if (!StringUtils.hasText(rawCategory)) {
            return properties.getDefaultCategory();
        }
        String key = rawCategory.trim().toLowerCase(Locale.ROOT);
        return properties.getCategoryMappings().getOrDefault(key, rawCategory);
    }

    public String resolveSourceAccount(String rawSourceAccount) {
        if (!StringUtils.hasText(rawSourceAccount)) {
            return properties.getDefaultSourceAccount();
        }
        return rawSourceAccount;
    }

    public String defaultDestinationAccount() {
        return properties.getDefaultDestinationAccount();
    }

    public String defaultCurrency() {
        return properties.getCurrency();
    }
}
