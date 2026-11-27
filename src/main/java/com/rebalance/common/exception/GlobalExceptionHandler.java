package com.rebalance.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Единая обработка ошибок REST API */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(UnauthorizedException.class)
	public ProblemDetail handleUnauthorizedException(UnauthorizedException ex) {
		return createProblemDetail(
				HttpStatus.UNAUTHORIZED,
				"Ошибка авторизации",
				"Неверные учетные данные или отсутствует действительная авторизация");
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
		return createProblemDetail(HttpStatus.NOT_FOUND, "Объект не найден", ex.getMessage());
	}

	@ExceptionHandler(ConflictException.class)
	public ProblemDetail handleConflictException(ConflictException ex) {
		return createProblemDetail(HttpStatus.CONFLICT, "Конфликт данных", ex.getMessage());
	}

	@ExceptionHandler(ExternalServiceUnavailableException.class)
	public ProblemDetail handleExternalServiceUnavailableException(ExternalServiceUnavailableException ex) {

		LOGGER.warn("Внешний сервис временно недоступен", ex);

		return createProblemDetail(
				HttpStatus.SERVICE_UNAVAILABLE,
				"Сервис временно недоступен",
				"Не удалось выполнить запрос к внешнему сервису. Повторите попытку позже");
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnexpectedException(Exception ex) {
		LOGGER.error("Непредвиденная ошибка при обработке запроса", ex);

		return createProblemDetail(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Внутренняя ошибка сервера",
				"Не удалось обработать запрос. Повторите попытку позже");
	}

	private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail) {

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);

		problemDetail.setTitle(title);

		return problemDetail;
	}
}
