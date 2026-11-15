package com.rebalance.service.auth;

import com.rebalance.dto.request.LoginRequest;
import com.rebalance.dto.request.RegisterRequest;
import com.rebalance.dto.response.LoginResponse;
import com.rebalance.dto.response.RegisterResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Сервис авторизации; бизнес-логика регистрации и входа будет добавлена в issue #12. */
@Service
public class AuthService {

	/** Регистрирует пользователя после подключения бизнес-логики. */
	public RegisterResponse register(RegisterRequest request) {
		throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Регистрация пока недоступна");
	}

	/** Авторизует пользователя после подключения бизнес-логики. */
	public LoginResponse login(LoginRequest request) {
		throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Вход пока недоступен");
	}
}
