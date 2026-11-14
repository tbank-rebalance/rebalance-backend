package com.rebalance.dto.request;

/**
 * Учётные данные, которые клиент передаёт для входа.
 *
 * @param email адрес электронной почты
 * @param password пароль пользователя
 */
public record LoginRequest(String email, String password) {}
