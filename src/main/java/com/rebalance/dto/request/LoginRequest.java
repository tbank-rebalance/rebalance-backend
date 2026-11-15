package com.rebalance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
		@Schema(
				description = "Email пользователя",
				example = "ivan@example.com",
				requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank
		@Email
		@Size(max = 255)
		String email,

		@Schema(description = "Пароль пользователя", example = "secret123", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank
		String password) {}
