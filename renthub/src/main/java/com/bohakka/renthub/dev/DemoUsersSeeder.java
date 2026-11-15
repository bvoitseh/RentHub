package com.bohakka.renthub.dev;

import com.bohakka.renthub.user.domain.OwnerType;
import com.bohakka.renthub.user.domain.Role;
import com.bohakka.renthub.user.domain.User;
import com.bohakka.renthub.user.repository.UserRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Demo accounts for every role, dev profile only (CLAUDE.md 2.4). Password for all: {@value #PASSWORD}. */
@Slf4j
@Component
@Profile("dev")
@Order(2)
@RequiredArgsConstructor
public class DemoUsersSeeder implements ApplicationRunner {

	public static final String PASSWORD = "Password123";
	private static final String CITY = "Львів";

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		String hash = passwordEncoder.encode(PASSWORD);
		seed("admin@renthub.local", "Адміністратор", "+380671000001", Role.ADMIN, null, null, null, hash);
		seed("owner@renthub.local", "Олена Коваль", "+380671000002", Role.OWNER, OwnerType.PRIVATE, null, null, hash);
		seed("company@renthub.local", "Ігор Мельник", "+380671000003", Role.OWNER, OwnerType.COMPANY,
			"ТОВ «Будпрокат»", "12345678", hash);
		seed("renter@renthub.local", "Андрій Шевчук", "+380671000004", Role.RENTER, null, null, null, hash);
		log.info("Demo accounts (dev): admin@, owner@, company@, renter@renthub.local — password {}", PASSWORD);
	}

	private void seed(String email, String name, String phone, Role role, OwnerType ownerType, String companyName,
			String edrpou, String passwordHash) {
		if (users.existsByEmail(email)) {
			return;
		}
		User user = new User(name, email, phone, passwordHash, role, CITY, clock.instant());
		user.markEmailVerified();
		if (ownerType != null) {
			user.assignOwnerType(ownerType, companyName, edrpou);
		}
		users.save(user);
	}
}
