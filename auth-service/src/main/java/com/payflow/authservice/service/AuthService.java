package com.payflow.authservice.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.payflow.authservice.dto.LoginRequest;
import com.payflow.authservice.dto.LoginResult;
import com.payflow.authservice.dto.RegisterRequest;
import com.payflow.authservice.entity.Role;
import com.payflow.authservice.entity.User;
import com.payflow.authservice.exception.InvalidCredentialsException;
import com.payflow.authservice.exception.ResourceAlreadyExistsException;
import com.payflow.authservice.repository.UserRepository;
import com.payflow.authservice.security.JwtService;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {

		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	public User register(RegisterRequest request) {

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new ResourceAlreadyExistsException("Email already registered");
		}

		User user = new User();

		user.setName(request.getName());
		user.setEmail(request.getEmail());

		// Never store the plain-text password
		user.setPassword(passwordEncoder.encode(request.getPassword()));

		// Every newly registered user gets USER role
		user.setRole(Role.USER);

		user.setEnabled(true);

		return userRepository.save(user);
	}

	// LOGIN
	public LoginResult login(LoginRequest request) {

		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

		if (!user.isEnabled()) {
			throw new InvalidCredentialsException("Invalid email or password");
		}

		boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());

		if (!passwordMatches) {
			throw new InvalidCredentialsException("Invalid email or password");
		}

		String token = jwtService.generateToken(
		        user.getId(),
		        user.getEmail(),
		        user.getRole().name()
		);
		return new LoginResult(token, user.getEmail(), user.getRole().name());
	}
}