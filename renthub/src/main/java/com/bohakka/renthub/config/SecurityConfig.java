package com.bohakka.renthub.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Security model (NFR-4): server-side session cookie (HttpOnly, SameSite=Lax, Secure in prod),
 * CSRF double-submit cookie for the same-origin SPA, BCrypt passwords, role checks via method security.
 * Authentication endpoints are added by the accounts feature; the skeleton only exposes public infrastructure paths.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private static final String[] DEV_TOOLS = {"/h2-console/**", "/swagger-ui/**", "/swagger-ui.html", "/api-docs/**"};

	private final Environment environment;
	private final SecurityProblemHandler problemHandler;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		boolean dev = environment.matchesProfiles("dev");
		http
			.csrf(csrf -> {
				csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
					.csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler());
				if (dev) {
					csrf.ignoringRequestMatchers("/h2-console/**");
				}
			})
			.headers(headers -> {
				if (dev) {
					headers.frameOptions(frame -> frame.sameOrigin());
				}
			})
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.requestCache(AbstractHttpConfigurer::disable)
			.exceptionHandling(handling -> handling
				.authenticationEntryPoint(problemHandler)
				.accessDeniedHandler(problemHandler))
			.authorizeHttpRequests(auth -> {
				auth.requestMatchers("/actuator/health").permitAll();
				if (dev) {
					auth.requestMatchers(DEV_TOOLS).permitAll();
				}
				auth.anyRequest().authenticated();
			});
		return http.build();
	}
}
