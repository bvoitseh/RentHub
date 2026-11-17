package com.bohakka.renthub.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** FR-2a: credentials of the single preset administrator (ADMIN_EMAIL, ADMIN_PASSWORD; ADMIN_NAME has a default). */
@ConfigurationProperties(prefix = "app.admin")
public record AdminBootstrapProperties(String email, String password, String name) {
}
