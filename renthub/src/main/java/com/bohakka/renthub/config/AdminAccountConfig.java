package com.bohakka.renthub.config;

import com.bohakka.renthub.entity.Role;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.properties.AdminBootstrapProperties;
import com.bohakka.renthub.repository.UserRepository;
import java.time.Clock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

/**
 * FR-2a: the application presets exactly one administrator account from ADMIN_EMAIL / ADMIN_PASSWORD (ADMIN_NAME is
 * optional). It is created at start-up, already verified, only when no ADMIN exists yet; registration never accepts
 * the ADMIN role, so this is the only way an administrator appears. The password is never logged.
 */
@Slf4j
@Configuration
public class AdminAccountConfig {

	@Bean
	ApplicationRunner adminAccountInitializer(AdminBootstrapProperties properties, UserRepository users,
			PasswordEncoder passwordEncoder, Clock clock, TransactionTemplate transaction) {
		return args -> transaction.executeWithoutResult(status -> {
			if (users.existsByRole(Role.ADMIN)) {
				log.debug("The administrator account already exists; nothing to preset");
				return;
			}
			if (!StringUtils.hasText(properties.email()) || !StringUtils.hasText(properties.password())) {
				log.warn("No administrator exists and ADMIN_EMAIL / ADMIN_PASSWORD are not set in .env; "
					+ "no administrator was created");
				return;
			}
			String email = User.normalizeEmail(properties.email());
			if (users.existsByEmail(email)) {
				log.warn("ADMIN_EMAIL {} already belongs to a non-admin account; no administrator was created", email);
				return;
			}
			User admin = new User(properties.name(), email, null, passwordEncoder.encode(properties.password()),
				Role.ADMIN, null, clock.instant());
			admin.markEmailVerified();
			users.save(admin);
			log.info("Created the administrator account {}", email);
		});
	}
}
