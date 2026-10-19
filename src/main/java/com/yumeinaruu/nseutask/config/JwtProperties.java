package com.yumeinaruu.nseutask.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "nseutask.jwt")
public record JwtProperties(
        @DefaultValue("dev-only-secret-817f14137cbc4b5dd9c1159c3c9b08ca2bb995b757b8d5a0") String secret,
        @DefaultValue("24h") Duration expiration,
        @DefaultValue("nseutask") String issuer
) {
}