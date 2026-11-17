package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.LoginRequest;
import com.bohakka.renthub.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * FR-2 login and FR-3c restore. Credentials are verified first (with NFR-14 limits per IP and per e-mail);
 * only then is the account state checked, so a wrong password never reveals whether an account is blocked
 * or deactivated.
 */
public interface LoginService {

	User login(LoginRequest request, HttpServletRequest http, HttpServletResponse response);

	/** FR-3c: a deactivated (not blocked) user with valid credentials restores the account and is logged in. */
	User restore(LoginRequest request, HttpServletRequest http, HttpServletResponse response);

	/** Opens a session for an already verified account (login, restore, auto-login after registration). */
	User startSession(User user, HttpServletRequest http, HttpServletResponse response);
}
