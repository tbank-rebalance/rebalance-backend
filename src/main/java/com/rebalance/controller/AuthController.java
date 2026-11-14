package com.rebalance.controller;

import com.rebalance.auth.AuthService;
import com.rebalance.dto.request.LoginRequest;
import com.rebalance.dto.request.RegisterRequest;
import com.rebalance.dto.response.LoginResponse;
import com.rebalance.dto.response.RegisterResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** HTTP-эндпоинты регистрации и входа пользователя. */
@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	/** Создаёт контроллер с реализацией бизнес-логики авторизации. */
	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	/** Регистрирует пользователя и возвращает публичные данные созданной учётной записи. */
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	/** Проверяет учётные данные и возвращает токен доступа. */
	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}
}
