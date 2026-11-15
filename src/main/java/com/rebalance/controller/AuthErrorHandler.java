package com.rebalance.controller;

import com.rebalance.auth.EmailAlreadyExistsException;
import com.rebalance.auth.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Преобразует ошибки запросов авторизации в единый формат API. */
@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthErrorHandler {

	private static final MediaType PROBLEM_JSON = MediaType.parseMediaType("application/problem+json");

	/** Возвращает 400 для нечитаемого тела или неверного типа поля. */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> malformedRequest(HttpServletRequest request) {
		return problem(
				HttpStatus.BAD_REQUEST,
				"malformed-request",
				"Некорректный запрос",
				"Проверьте формат запроса",
				"MALFORMED_REQUEST",
				request,
				List.of());
	}

	/** Возвращает 400 для незаполненного поля и 422 для недопустимого значения. */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> invalidFields(
			MethodArgumentNotValidException exception, HttpServletRequest request) {
		Map<String, Map<String, Object>> fieldErrors = new LinkedHashMap<>();
		boolean missing = false;
		for (FieldError error : exception.getBindingResult().getFieldErrors()) {
			Object value = error.getRejectedValue();
			boolean blank = value == null || value instanceof String text && text.isBlank();
			missing |= blank;
			Map<String, Object> fieldError = new LinkedHashMap<>();
			fieldError.put("field", error.getField());
			fieldError.put("message", blank ? "Поле обязательно для заполнения" : error.getDefaultMessage());
			if (!blank && !"password".equals(error.getField())) {
				fieldError.put("rejectedValue", value);
			}
			fieldErrors.put(error.getField(), fieldError);
		}
		if (missing) {
			return problem(
					HttpStatus.BAD_REQUEST,
					"missing-field",
					"Не заполнены обязательные поля",
					"Заполните обязательные поля",
					"MISSING_FIELD",
					request,
					new ArrayList<>(fieldErrors.values()));
		}
		return problem(
				HttpStatus.UNPROCESSABLE_CONTENT,
				"validation-failed",
				"Некорректные данные",
				"Проверьте поля с ошибками",
				"VALIDATION_FAILED",
				request,
				new ArrayList<>(fieldErrors.values()));
	}

	/** Возвращает общую ошибку для неизвестного email и неверного пароля. */
	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<Map<String, Object>> invalidCredentials(HttpServletRequest request) {
		return problem(
				HttpStatus.UNAUTHORIZED,
				"invalid-credentials",
				"Неверные учётные данные",
				"Неверный email или пароль",
				"INVALID_CREDENTIALS",
				request,
				null);
	}

	/** Возвращает конфликт для повторной регистрации email. */
	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<Map<String, Object>> emailAlreadyExists(HttpServletRequest request) {
		return problem(
				HttpStatus.CONFLICT,
				"email-already-exists",
				"Email уже используется",
				"Пользователь с таким email уже зарегистрирован",
				"EMAIL_ALREADY_EXISTS",
				request,
				null);
	}

	private static ResponseEntity<Map<String, Object>> problem(
			HttpStatus status,
			String type,
			String title,
			String detail,
			String code,
			HttpServletRequest request,
			List<Map<String, Object>> errors) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("type", "https://rebalance/errors/" + type);
		body.put("title", title);
		body.put("status", status.value());
		body.put("detail", detail);
		body.put("instance", request.getRequestURI());
		body.put("code", code);
		body.put("timestamp", OffsetDateTime.now().toString());
		if (errors != null) {
			body.put("errors", errors);
		}
		return ResponseEntity.status(status).contentType(PROBLEM_JSON).body(body);
	}
}
