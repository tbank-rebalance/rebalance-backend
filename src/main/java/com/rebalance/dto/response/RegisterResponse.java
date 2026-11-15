package com.rebalance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Данные созданного пользователя, возвращаемые после регистрации.
 *
 * @param userId идентификатор пользователя
 * @param email адрес электронной почты
 * @param firstName имя пользователя
 * @param lastName фамилия пользователя
 */
public record RegisterResponse(
		@Schema(description = "Идентификатор созданного пользователя", example = "42")
		Integer userId,

		@Schema(description = "Email пользователя", example = "ivan@example.com")
		String email,

		@Schema(description = "Имя пользователя", example = "Иван")
		String firstName,

		@Schema(description = "Фамилия пользователя", example = "Иванов")
		String lastName) {}
