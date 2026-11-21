package com.rebalance.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	// Обработка исключений 404, 409 и тд
	@ExceptionHandler(ResponseStatusException.class)
	public ProblemDetail handleResponseStatusException(ResponseStatusException e) {
		return e.getBody();
	}

	// Обработка исключений валидации (400)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidationException(MethodArgumentNotValidException e) {
		ProblemDetail problemDetail =
				ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Проверьте правильность заполнения полей");
		problemDetail.setTitle("Некорректные данные");
		return problemDetail;
	}

	// Обработка всех остальных ошибок (500)
	@ExceptionHandler(Exception.class)
	public ProblemDetail handleGlobalException(Exception e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось обработать запрос. Повторите попытку позже");
		problemDetail.setTitle("Внутренняя ошибка сервера");
		return problemDetail;
	}
}
