package com.rebalance.dto.response;

/**
 * Данные созданного пользователя, возвращаемые после регистрации.
 *
 * @param userId идентификатор пользователя
 * @param email адрес электронной почты
 * @param firstName имя пользователя
 * @param lastName фамилия пользователя
 */
public record RegisterResponse(Integer userId, String email, String firstName, String lastName) {}
