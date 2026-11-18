package com.rebalance.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// Обработка бизнес-ошибок
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ProblemDetail> handleResponseStatusException(
			ResponseStatusException e, HttpServletRequest request) {
		HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
		ErrorCode errorCode = ErrorCode.fromStatus(status);
		String code = errorCode.code;
		String title = errorCode.title;
		String detail = errorCode.detail;

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
		problemDetail.setType(
				URI.create("https://rebalance/errors/" + code.toLowerCase().replace('_', '-')));
		problemDetail.setTitle(title);
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("code", code);
		problemDetail.setProperty("timestamp", OffsetDateTime.now());

		if (errorCode == ErrorCode.BUSINESS_ERROR || errorCode == ErrorCode.UNPROCESSABLE_CONTENT) {
			problemDetail.setProperty("errors", List.of());
		}

		ResponseEntity.BodyBuilder response =
				ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON);

		if (errorCode == ErrorCode.SERVICE_UNAVAILABLE) {
			response.header(HttpHeaders.RETRY_AFTER, "30");
		}
		return response.body(problemDetail);
	}

	// Обработка ошибок валидации
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetail> handleValidationException(
			MethodArgumentNotValidException e, HttpServletRequest request) {
		ErrorCode errorCode = ErrorCode.UNPROCESSABLE_CONTENT;
		ProblemDetail problemDetail =
				ProblemDetail.forStatusAndDetail(errorCode.status, "Проверьте правильность заполненных полей");
		problemDetail.setType(URI.create(
				"https://rebalance/errors/" + errorCode.code.toLowerCase().replace('_', '-')));
		problemDetail.setTitle(errorCode.title);
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("code", errorCode.code);
		problemDetail.setProperty("timestamp", OffsetDateTime.now());
		List<Map<String, String>> errors = e.getBindingResult().getFieldErrors().stream()
				.map(error -> Map.of(
						"field",
						error.getField(),
						"message",
						error.getDefaultMessage() != null ? error.getDefaultMessage() : "Некорректное значение"))
				.toList();
		problemDetail.setProperty("errors", errors);
		return ResponseEntity.status(errorCode.status)
				.contentType(MediaType.APPLICATION_PROBLEM_JSON)
				.body(problemDetail);
	}

	// Обработка всех остальных непредвиденных ошибок
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ProblemDetail> handleGlobalException(Exception e, HttpServletRequest request) {

		ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.status, errorCode.detail);
		problemDetail.setType(URI.create(
				"https://rebalance/errors/" + errorCode.code.toLowerCase().replace('_', '-')));
		problemDetail.setTitle(errorCode.title);
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("code", errorCode.code);
		problemDetail.setProperty("timestamp", OffsetDateTime.now());

		return ResponseEntity.status(errorCode.status)
				.contentType(MediaType.APPLICATION_PROBLEM_JSON)
				.body(problemDetail);
	}
}
