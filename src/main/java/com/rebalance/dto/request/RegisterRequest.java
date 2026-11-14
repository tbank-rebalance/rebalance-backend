package com.rebalance.dto.request;

/**
 * Учётные данные, которые клиент передаёт для регистрации пользователя.
 *
 * @param email адрес электронной почты
 * @param password пароль в открытом виде
 * @param firstName имя пользователя
 * @param lastName фамилия пользователя
 */
public record RegisterRequest(String email, String password, String firstName, String lastName) {}
