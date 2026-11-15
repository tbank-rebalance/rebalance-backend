package com.rebalance.controller.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rebalance.dto.request.LoginRequest;
import com.rebalance.dto.request.RegisterRequest;
import com.rebalance.dto.response.LoginResponse;
import com.rebalance.dto.response.RegisterResponse;
import com.rebalance.service.auth.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Проверки HTTP-контракта регистрации и входа. TODO(tolstovrob): без реализации бизнес-логики. */
@WebMvcTest(AuthController.class)
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
	void rejectsRequestWithMissingRequiredFields() throws Exception {
		mvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"ivan@example.com\",\"password\":\"secret123\",\"firstName\":\" \"}"))
				.andExpect(status().isBadRequest());
	}
}
