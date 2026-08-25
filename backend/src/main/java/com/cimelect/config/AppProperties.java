package com.cimelect.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Upload upload,
        Shipment shipment,
        Bootstrap bootstrap
) {
    public record Jwt(String secret, long expirationMs) {
    }

    public record Upload(String dir) {
    }

    public record Shipment(int delayThresholdDays) {
    }

    public record Bootstrap(Admin admin) {
        public record Admin(String email, String password) {
        }
    }
}
