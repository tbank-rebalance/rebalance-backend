package com.rebalance.common.exception;

/** Ошибка отсутствующей или недействительной авторизации */
public class UnauthorizedException extends RuntimeException {

	/** Создает исключение с общим сообщением об ошибке авторизации */
	public UnauthorizedException() {
		super("Неверные учетные данные или отсутствует действительная авторизация");
	}
}
