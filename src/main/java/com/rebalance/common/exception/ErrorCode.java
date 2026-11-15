package com.rebalance.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Требуется авторизация", "Необходимо войти в систему"),
	FORBIDDEN(
			HttpStatus.FORBIDDEN, "FORBIDDEN", "Доступ запрещен", "У вас нет разрешения на выполнение этого действия"),
	NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "Ресурс не найден", "Запрашиваемый ресурс не найден"),
	CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "Конфликт данных", "Запрос конфликтует с существующими данными"),
	UNPROCESSABLE_CONTENT(
			HttpStatus.UNPROCESSABLE_CONTENT,
			"VALIDATION_FAILED",
			"Некорректные данные",
			"Проверьте правильность переданных данных"),
	BAD_GATEWAY(
			HttpStatus.BAD_GATEWAY,
			"BAD_GATEWAY",
			"Ошибка внешнего сервиса",
			"Получен некорректный ответ от банковского сервиса"),
	SERVICE_UNAVAILABLE(
			HttpStatus.SERVICE_UNAVAILABLE,
			"SERVICE_UNAVAILABLE",
			"Сервис временно недоступен",
			"Не удалось связаться с банковским сервисом"),
	INTERNAL_ERROR(
			HttpStatus.INTERNAL_SERVER_ERROR,
			"INTERNAL_ERROR",
			"Внутренняя ошибка сервера",
			"Не удалось обработать запрос"),
	BUSINESS_ERROR(HttpStatus.BAD_REQUEST, "BUSINESS_ERROR", "Ошибка обработки запроса", "Не удалось выполнить запрос");

	public final HttpStatus status;
	public final String code, title, detail;

	ErrorCode(HttpStatus status, String code, String title, String detail) {
		this.status = status;
		this.code = code;
		this.title = title;
		this.detail = detail;
	}
	// Метод для поиска по статусу
	public static ErrorCode fromStatus(HttpStatus status) {
		for (ErrorCode errorCode : values()) {
			if (errorCode.status == status) {
				return errorCode;
			}
		}
		return INTERNAL_ERROR; // Возвращаем INTERNAL_ERROR по умолчанию, если статус не найден
	}
}
