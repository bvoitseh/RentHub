package com.bohakka.renthub.service;

import com.bohakka.renthub.config.AdminBootstrapProperties;
import com.bohakka.renthub.entity.Role;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.repository.UserRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * FR-2a: at start-up creates the first ADMIN (already verified) from environment variables when no admin exists.
 * Without the variables only a warning is logged. The password is never logged.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

	private final AdminBootstrapProperties properties;
	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (users.existsByRole(Role.ADMIN)) {
			log.debug("An administrator already exists; skipping admin bootstrap");
			return;
		}
		if (!StringUtils.hasText(properties.email()) || !StringUtils.hasText(properties.password())
				|| !StringUtils.hasText(properties.name())) {
			log.warn("No administrator exists and ADMIN_EMAIL / ADMIN_PASSWORD / ADMIN_NAME are not all set; "
				+ "no administrator was created");
			return;
		}
		User admin = new User(properties.name(), properties.email(), null,
			passwordEncoder.encode(properties.password()), Role.ADMIN, null, clock.instant());
		admin.markEmailVerified();
		users.save(admin);
		log.info("Created the first administrator account {}", admin.getEmail());
	}
}
