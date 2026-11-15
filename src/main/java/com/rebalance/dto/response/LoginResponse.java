package com.rebalance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Токен доступа, возвращаемый после успешного входа.
 *
 * @param accessToken токен доступа
 * @param tokenType   тип токена для заголовка Authorization
 */
public record LoginResponse(
		@Schema(description = "Токен доступа", example = "eyJhbGciOiJIUzI1NiJ9...")
		String accessToken,

		@Schema(description = "Тип токена для заголовка Authorization", example = "Bearer")
		String tokenType) {}
