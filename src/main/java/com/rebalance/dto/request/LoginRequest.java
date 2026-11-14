package com.rebalance.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Учётные данные, которые клиент передаёт для входа.
 *
 * @param email адрес электронной почты
 * @param password пароль пользователя
 */
public record LoginRequest(
		@NotBlank @Email @Size(max = 255) String email,
		@NotBlank String password) {}
