package com.rebalance.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TestErrorController.class)
public class GlobalExceptionHandlerTest {
	@Autowired
	private MockMvc mvc;

	// Проверка обработки ошибки 404 через ResponseStatusException
	@Test
	void shouldReturn404WithProblemDetail() throws Exception {
		mvc.perform(get("/test-errors/not-found"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.title").value("Not Found"));
	}

	// Проверка обработки ошибки 409 через ResponseStatusException
	@Test
	void shouldReturn409WithProblemDetail() throws Exception {
		mvc.perform(get("/test-errors/conflict"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.title").value("Conflict"));
	}

	// Проверка обработки внутренней ошибки 500 без раскрытия технических деталей
	@Test
	void shouldReturn500WithoutTechnicalDetails() throws Exception {
		mvc.perform(get("/test-errors/internal"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.status").value(500))
				.andExpect(jsonPath("$.title").value("Внутренняя ошибка сервера"))
				.andExpect(jsonPath("$.detail").value("Не удалось обработать запрос. Повторите попытку позже"));
	}

	// Проверка возврата 404 при обращении к несуществующему URL
	@Test
	void shouldReturn404ForUnknownUrl() throws Exception {
		mvc.perform(get("/unknown-review"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	// Проверка возврата 405 и заголовка Allow при использовании неподдерживаемого HTTP-метода
	@Test
	void shouldReturn405WithAllowHeader() throws Exception {
		mvc.perform(get("/test-errors/post-only"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(header().exists("Allow"))
				.andExpect(jsonPath("$.status").value(405));
	}

	// Проверка возврата 400 при отправке некорректного JSON
	@Test
	void shouldReturn400ForMalformedJson() throws Exception {
		mvc.perform(post("/test-errors/json-test")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"broken\": "))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	// Проверка возврата 401 при отсутствии действительной авторизации
	@Test
	void shouldReturn401ForUnauthorizedException() throws Exception {
		mvc.perform(get("/test-errors/unauthorized"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.detail")
						.value("Неверные учетные данные или отсутствует действительная авторизация"));
	}

	// Проверка возврата 404 при отсутствии запрашиваемого объекта
	@Test
	void shouldReturn404ForResourceNotFoundException() throws Exception {
		mvc.perform(get("/test-errors/resource-not-found"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	// Проверка возврата 409 при конфликте бизнес-данных
	@Test
	void shouldReturn409ForConflictException() throws Exception {
		mvc.perform(get("/test-errors/business-conflict"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	// Проверка возврата 503 при временной недоступности внешнего сервиса
	@Test
	void shouldReturn503ForExternalServiceUnavailableException() throws Exception {
		mvc.perform(get("/test-errors/service-unavailable"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.status").value(503))
				.andExpect(jsonPath("$.detail")
						.value("Не удалось выполнить запрос к внешнему сервису. Повторите попытку позже"));
	}
}
