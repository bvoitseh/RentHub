package com.bohakka.renthub.user.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** FR-2a: optional first-administrator credentials from ADMIN_EMAIL / ADMIN_PASSWORD / ADMIN_NAME. */
@ConfigurationProperties(prefix = "app.admin")
public record AdminBootstrapProperties(String email, String password, String name) {
}
