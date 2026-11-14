package com.rebalance.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rebalance.auth.AuthService;
import com.rebalance.auth.EmailAlreadyExistsException;
import com.rebalance.auth.InvalidCredentialsException;
import com.rebalance.dto.request.LoginRequest;
import com.rebalance.dto.request.RegisterRequest;
import com.rebalance.dto.response.LoginResponse;
import com.rebalance.dto.response.RegisterResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Проверки HTTP-контракта регистрации и входа без реализации бизнес-логики. */
@WebMvcTest(AuthController.class)
@Import({AuthErrorHandler.class, AuthJsonConfiguration.class})
class AuthControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private AuthService authService;

	@Test
	void returnsCreatedUserAndAccessToken() throws Exception {
		when(authService.register(any(RegisterRequest.class)))
				.thenReturn(new RegisterResponse(42, "ivan@example.com", "Иван", "Иванов"));
		when(authService.login(any(LoginRequest.class))).thenReturn(new LoginResponse("test-token", "Bearer"));

		mvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
							{"email":"ivan@example.com","password":"secret123","firstName":"Иван","lastName":"Иванов"}
							"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.userId").value(42));

		mvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"ivan@example.com\",\"password\":\"secret123\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("test-token"));
	}

	@Test
	void returnsMissingFieldForAbsentAndBlankValues() throws Exception {
		mvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"ivan@example.com\",\"password\":\"secret123\",\"firstName\":\" \"}"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("application/problem+json")))
				.andExpect(jsonPath("$.code").value("MISSING_FIELD"))
				.andExpect(jsonPath("$.errors.length()").value(2));
	}

	@Test
	void returnsValidationFailedWithoutExposingPassword() throws Exception {
		mvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
							{"email":"invalid","password":"abcdefgh","firstName":"Иван","lastName":"Иванов"}
							"""))
				.andExpect(status().is(422))
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
				.andExpect(jsonPath("$.errors[?(@.field == 'password')].rejectedValue")
						.doesNotExist());
	}

	@Test
	void returnsMalformedRequestForWrongJsonType() throws Exception {
		mvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":123,\"password\":\"secret123\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
	}

	@Test
	void returnsConflictAndGenericUnauthorizedError() throws Exception {
		when(authService.register(any(RegisterRequest.class))).thenThrow(new EmailAlreadyExistsException());
		when(authService.login(any(LoginRequest.class))).thenThrow(new InvalidCredentialsException());

		mvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
							{"email":"ivan@example.com","password":"secret123","firstName":"Иван","lastName":"Иванов"}
							"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));

		mvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"ivan@example.com\",\"password\":\"wrong\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
				.andExpect(jsonPath("$.detail").value("Неверный email или пароль"));
	}
}
