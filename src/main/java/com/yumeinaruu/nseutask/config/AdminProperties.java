package com.yumeinaruu.nseutask.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "nseutask.admin")
public record AdminProperties(
        // в проде меняется)
        @DefaultValue("admin") String username,
        @DefaultValue("admin@example.com") String email,
        @DefaultValue("12345678") String password
) {
}
