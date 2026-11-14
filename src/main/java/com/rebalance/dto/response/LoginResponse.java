package com.rebalance.dto.response;

/**
 * Токен доступа, возвращаемый после успешного входа.
 *
 * @param accessToken токен доступа
 * @param tokenType тип токена для заголовка Authorization
 */
public record LoginResponse(String accessToken, String tokenType) {}
