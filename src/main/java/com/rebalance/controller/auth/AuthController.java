package com.rebalance.controller.auth;

import com.rebalance.dto.request.LoginRequest;
import com.rebalance.dto.request.RegisterRequest;
import com.rebalance.dto.response.LoginResponse;
import com.rebalance.dto.response.RegisterResponse;
import com.rebalance.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** HTTP-эндпоинты регистрации и входа пользователя. */
@RestController
@RequestMapping("/auth")
@Tag(name = "Авторизация", description = "Регистрация пользователя и вход в приложение")
public class AuthController {

	private final AuthService authService;

	/** Создаёт контроллер с сервисом авторизации. */
	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	/** Регистрирует пользователя и возвращает публичные данные созданной учётной записи. */
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Зарегистрировать пользователя", description = "Создаёт учётную запись с уникальным email")
	@ApiResponses({
		@ApiResponse(
				responseCode = "201",
				description = "Пользователь создан",
				content =
						@Content(
								mediaType = "application/json",
								schema = @Schema(implementation = RegisterResponse.class))),
		@ApiResponse(responseCode = "400", description = "Не заполнены обязательные поля или некорректный JSON"),
		@ApiResponse(responseCode = "409", description = "Email уже зарегистрирован"),
		@ApiResponse(responseCode = "422", description = "Нарушены правила валидации"),
		@ApiResponse(responseCode = "501", description = "Регистрация ещё не реализована")
	})
	public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	/** Проверяет учётные данные и возвращает токен доступа. */
	@PostMapping("/login")
	@Operation(summary = "Войти в приложение", description = "Проверяет email и пароль, возвращает токен доступа")
	@ApiResponses({
		@ApiResponse(
				responseCode = "200",
				description = "Вход выполнен",
				content =
						@Content(
								mediaType = "application/json",
								schema = @Schema(implementation = LoginResponse.class))),
		@ApiResponse(responseCode = "400", description = "Не заполнены обязательные поля или некорректный JSON"),
		@ApiResponse(responseCode = "401", description = "Неверный email или пароль"),
		@ApiResponse(responseCode = "422", description = "Нарушены правила валидации"),
		@ApiResponse(responseCode = "501", description = "Вход ещё не реализован")
	})
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}
}
