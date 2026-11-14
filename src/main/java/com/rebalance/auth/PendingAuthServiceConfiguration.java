package com.rebalance.auth;

import com.rebalance.dto.request.LoginRequest;
import com.rebalance.dto.request.RegisterRequest;
import com.rebalance.dto.response.LoginResponse;
import com.rebalance.dto.response.RegisterResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Временная реализация до подключения бизнес-логики авторизации в issue #12. */
@Configuration(proxyBeanMethods = false)
public class PendingAuthServiceConfiguration {

	/** Позволяет запустить приложение до появления реального сервиса авторизации. */
	@Bean
	@ConditionalOnMissingBean(AuthService.class)
	AuthService pendingAuthService() {
		return new AuthService() {
			@Override
			public RegisterResponse register(RegisterRequest request) {
				throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Регистрация пока недоступна");
			}

			@Override
			public LoginResponse login(LoginRequest request) {
				throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Вход пока недоступен");
			}
		};
	}
}
