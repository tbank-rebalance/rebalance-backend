package com.rebalance.dto.request;

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
		@NotBlank @Email @Size(max = 255) String email,

		@NotBlank @Size(min = 8, max = 64) @Pattern(regexp = "(?s)^(?=.*\\p{L})(?=.*\\p{Nd}).+$")
		String password,

		@NotBlank @Size(max = 100) String firstName,
		@NotBlank @Size(max = 100) String lastName) {}
