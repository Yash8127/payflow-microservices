package com.payflow.accountservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.payflow.accountservice.security.InternalServiceAuthenticationFilter;
import com.payflow.accountservice.security.JwtAuthenticationFilter;
import com.payflow.accountservice.security.RestAccessDeniedHandler;
import com.payflow.accountservice.security.RestAuthenticationEntryPoint;

@Configuration
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final RestAuthenticationEntryPoint authenticationEntryPoint;
	private final RestAccessDeniedHandler accessDeniedHandler;
	private final InternalServiceAuthenticationFilter internalServiceAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
			RestAuthenticationEntryPoint authenticationEntryPoint, RestAccessDeniedHandler accessDeniedHandler,
			InternalServiceAuthenticationFilter internalServiceAuthenticationFilter) {

		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.authenticationEntryPoint = authenticationEntryPoint;
		this.accessDeniedHandler = accessDeniedHandler;
		this.internalServiceAuthenticationFilter = internalServiceAuthenticationFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable())

				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))

				.authorizeHttpRequests(auth -> auth

						// Internal service-to-service APIs
						.requestMatchers("/internal/accounts/**").hasRole("SERVICE")

						// Account creation
						.requestMatchers("/accounts").hasAnyRole("USER", "ADMIN")

						// User account access
						.requestMatchers("/accounts/user/**", "/accounts/{id}", "/accounts/number/**",
								"/accounts/*/deposit", "/accounts/*/withdraw")
						.hasAnyRole("USER", "ADMIN")

						// Admin account management
						.requestMatchers("/accounts/*/freeze", "/accounts/*/unfreeze").hasRole("ADMIN")

						.anyRequest().authenticated())

				.addFilterBefore(internalServiceAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}