package com.rebalance.auth;

import com.rebalance.dto.request.LoginRequest;
import com.rebalance.dto.request.RegisterRequest;
import com.rebalance.dto.response.LoginResponse;
import com.rebalance.dto.response.RegisterResponse;

/** Интерфейс между HTTP-контроллером и бизнес-логикой авторизации. */
public interface AuthService {

	/** Создаёт пользователя и возвращает его публичные данные. */
	RegisterResponse register(RegisterRequest request);

	/** Проверяет учётные данные и возвращает токен доступа. */
	LoginResponse login(LoginRequest request);
}
