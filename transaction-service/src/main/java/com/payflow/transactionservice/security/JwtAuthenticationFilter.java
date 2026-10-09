package com.payflow.transactionservice.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authorizationHeader = request.getHeader("Authorization");

		// No JWT → continue the filter chain
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {

			filterChain.doFilter(request, response);
			return;
		}

		String token = authorizationHeader.substring(7);

		try {

			if (!jwtService.isTokenValid(token)) {
				filterChain.doFilter(request, response);
				return;
			}

			String email = jwtService.extractEmail(token);
			String role = jwtService.extractRole(token);
			Long userId = jwtService.extractUserId(token);

			SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null,
					List.of(authority));

			// Store email as authentication details
			authentication.setDetails(email);

			SecurityContextHolder.getContext().setAuthentication(authentication);

		} catch (Exception e) {

			// Invalid/tampered JWT
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}
}