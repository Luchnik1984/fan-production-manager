package com.fanproduction.api.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private Long expiration;

    public void setSecret(String secret) { this.secret = secret; }

    public void setExpiration(Long expiration) { this.expiration = expiration; }
}
