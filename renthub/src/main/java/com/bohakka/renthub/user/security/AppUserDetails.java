package com.bohakka.renthub.user.security;

import com.bohakka.renthub.user.domain.Role;
import com.bohakka.renthub.user.domain.User;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Session principal: a small immutable snapshot of the account. Equality is by user id so that
 * {@link org.springframework.security.core.session.SessionRegistry} can find every session of a user.
 * Account status (blocked / deactivated) is checked after password verification in LoginService,
 * so a wrong password never reveals the account state.
 */
@Getter
public final class AppUserDetails implements UserDetails {

	private final Long id;
	private final String email;
	private final String passwordHash;
	private final Role role;
	private final boolean blocked;
	private final boolean deactivated;
	private final boolean emailVerified;

	public AppUserDetails(User user) {
		this.id = user.getId();
		this.email = user.getEmail();
		this.passwordHash = user.getPasswordHash();
		this.role = user.getRole();
		this.blocked = user.isBlocked();
		this.deactivated = user.isDeactivated();
		this.emailVerified = user.isEmailVerified();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof AppUserDetails that && id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return "AppUserDetails[id=" + id + ", role=" + role + "]";
	}
}
