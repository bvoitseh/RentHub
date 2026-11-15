package com.bohakka.renthub.user.security;

import com.bohakka.renthub.user.domain.User;
import com.bohakka.renthub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

	private final UserRepository users;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) {
		return users.findByEmail(User.normalizeEmail(email))
			.map(AppUserDetails::new)
			.orElseThrow(() -> new UsernameNotFoundException("No user with e-mail " + email));
	}
}
