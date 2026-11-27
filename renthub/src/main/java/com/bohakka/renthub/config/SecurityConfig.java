package com.bohakka.renthub.config;

import com.bohakka.renthub.security.SecurityProblemHandler;
import com.bohakka.renthub.security.SpaCsrfTokenRequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

/**
 * Security model (NFR-4): server-side session cookie (HttpOnly, SameSite=Lax), CSRF double-submit
 * cookie for the same-origin SPA, BCrypt passwords, role checks via method security. Login is a REST endpoint
 * (see LoginService); logout is Spring Security's filter. Sessions are tracked in an in-memory registry so a
 * password reset, deactivation or admin block can end every session of a user.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private static final String[] DEV_TOOLS = {"/h2-console/**"};
	/** FR-6, FR-7, FR-19: the catalogue, cards, categories and public profiles are open to guests. */
	private static final String[] PUBLIC_GET = {"/api/v1/categories", "/api/v1/equipment", "/api/v1/equipment/{id:\\d+}",
		"/api/v1/equipment/{id:\\d+}/availability", "/api/v1/users/{id:\\d+}",
		"/api/v1/users/{id:\\d+}/reviews", "/api/v1/recommendations", "/api/v1/regions", "/api/v1/settlements"};
	private static final String[] PUBLIC_AUTH_POST = {"/api/v1/auth/register", "/api/v1/auth/login",
		"/api/v1/auth/restore", "/api/v1/auth/verify-email", "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password"};

	private final SecurityProblemHandler problemHandler;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}

	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository contextRepository,
			SessionRegistry sessionRegistry) throws Exception {
		SessionInformationExpiredStrategy expired = event -> problemHandler.commence(event.getRequest(),
			event.getResponse(), new SessionAuthenticationException("Session expired"));
		http
			.csrf(csrf -> {
				csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
					.csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler());
				csrf.ignoringRequestMatchers("/h2-console/**");
			})
			.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
			.securityContext(context -> context.securityContextRepository(contextRepository))
			.sessionManagement(session -> {
				session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
				session.maximumSessions(-1).sessionRegistry(sessionRegistry).expiredSessionStrategy(expired);
			})
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.requestCache(AbstractHttpConfigurer::disable)
			.logout(logout -> logout
				.logoutUrl("/api/v1/auth/logout")
				.logoutSuccessHandler((request, response, auth) -> response.setStatus(HttpStatus.NO_CONTENT.value())))
			.exceptionHandling(handling -> handling
				.authenticationEntryPoint(problemHandler)
				.accessDeniedHandler(problemHandler))
			.authorizeHttpRequests(auth -> {
				auth.requestMatchers("/actuator/health").permitAll();
				auth.requestMatchers(HttpMethod.POST, PUBLIC_AUTH_POST).permitAll();
				auth.requestMatchers(HttpMethod.GET, PUBLIC_GET).permitAll();
				auth.requestMatchers(DEV_TOOLS).permitAll();
				auth.anyRequest().authenticated();
			});
		return http.build();
	}
}
