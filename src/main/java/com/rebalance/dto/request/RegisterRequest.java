package com.rebalance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Учётные данные, которые клиент передаёт для регистрации пользователя.
 *
 * @param email адрес электронной почты
 * @param password пароль в открытом виде
 * @param firstName имя пользователя
 * @param lastName фамилия пользователя
 */
public record RegisterRequest(
		@Schema(
				description = "Email пользователя",
				example = "ivan@example.com",
				requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank
		@Email
		@Size(max = 255)
		String email,

		@Schema(
				description = "Пароль: 8–64 символа, минимум одна буква и одна цифра",
				example = "secret123",
				requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank
		@Size(min = 8, max = 64)
		@Pattern(regexp = "(?s)^(?=.*\\p{L})(?=.*\\p{Nd}).+$")
		String password,

		@Schema(description = "Имя пользователя", example = "Иван", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank
		@Size(max = 100)
		String firstName,

		@Schema(description = "Фамилия пользователя", example = "Иванов", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank
		@Size(max = 100)
		String lastName) {}
