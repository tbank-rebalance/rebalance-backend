package com.rebalance.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rebalance.TestcontainersConfiguration;
import com.rebalance.entity.user.User;
import com.rebalance.repository.user.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

/** Сквозные проверки регистрации, входа и доступа по токену. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, AuthFlowTests.ProtectedController.class})
class AuthFlowTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper json;

	@Autowired
	private UserRepository users;

	@Autowired
	private JwtDecoder decoder;

	@Autowired
	private PasswordEncoder passwords;

	@Autowired
	private JwtEncoder encoder;

	/** Удаляет пользователей, созданных предыдущими тестами. */
	@BeforeEach
	void clearDatabase() {
		users.deleteAll();
	}

	/** Проверяет хеширование пароля и уникальность email без учёта регистра. */
	@Test
	void registersUserWithHashedPasswordAndRejectsCaseInsensitiveDuplicate() throws Exception {
		mvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(registration("Ivan@Example.com")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("Ivan@Example.com"))
				.andExpect(jsonPath("$.firstName").value("Иван"));

		User saved = users.findByEmailIgnoreCase("ivan@example.com").orElseThrow();
		assertThat(saved.getPasswordHash()).isNotEqualTo("secret123");
		assertThat(saved.getPasswordHash()).startsWith("{pbkdf2@SpringSecurity_v5_8}");
		assertThat(passwords.matches("secret123", saved.getPasswordHash())).isTrue();

		mvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(registration("ivan@example.com")))
				.andExpect(status().isConflict());
		assertThat(users.count()).isEqualTo(1);
	}

	/** Проверяет вход и доступ к закрытому маршруту с действительным и просроченным JWT. */
	@Test
	void logsInAndProtectsDataWithExpiringBearerToken() throws Exception {
		mvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(registration("ivan@example.com")))
				.andExpect(status().isCreated());

		mvc.perform(get("/test/protected")).andExpect(status().isUnauthorized());
		mvc.perform(get("/test/protected").header("Authorization", "Bearer invalid"))
				.andExpect(status().isUnauthorized());

		MvcResult login = mvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"IVAN@example.com\",\"password\":\"secret123\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andReturn();
		String token = json.readTree(login.getResponse().getContentAsString())
				.get("accessToken")
				.stringValue();
		assertThat(token).isNotBlank();
		assertThat(decoder.decode(token).getSubject())
				.isEqualTo(users.findByEmailIgnoreCase("ivan@example.com")
						.orElseThrow()
						.getId()
						.toString());

		mvc.perform(get("/test/protected").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		Instant past = Instant.now().minusSeconds(120);
		String expiredToken = encoder.encode(JwtEncoderParameters.from(
						JwsHeader.with(MacAlgorithm.HS256).build(),
						JwtClaimsSet.builder()
								.issuer("rebalance")
								.subject("1")
								.issuedAt(past.minusSeconds(60))
								.expiresAt(past)
								.build()))
				.getTokenValue();
		mvc.perform(get("/test/protected").header("Authorization", "Bearer " + expiredToken))
				.andExpect(status().isUnauthorized());
	}

	/** Проверяет одинаковый ответ при неизвестном email и неверном пароле. */
	@Test
	void returnsSameUnauthorizedResponseForUnknownEmailAndWrongPassword() throws Exception {
		mvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(registration("ivan@example.com")))
				.andExpect(status().isCreated());

		MvcResult unknown = mvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"nobody@example.com\",\"password\":\"wrong-password\"}"))
				.andExpect(status().isUnauthorized())
				.andReturn();
		MvcResult wrong = mvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"ivan@example.com\",\"password\":\"wrong-password\"}"))
				.andExpect(status().isUnauthorized())
				.andReturn();
		assertThat(unknown.getResponse().getContentAsString())
				.isEqualTo(wrong.getResponse().getContentAsString());
		assertThat(json.readTree(unknown.getResponse().getContentAsString())
						.get("detail")
						.stringValue())
				.isEqualTo("Неверный email или пароль");
	}

	/**
	 * Создаёт JSON-запрос регистрации с тестовыми данными.
	 *
	 * @param email адрес пользователя
	 * @return JSON-тело запроса
	 */
	private static String registration(String email) {
		return "{\"email\":\"" + email
				+ "\",\"password\":\"secret123\",\"firstName\":\"Иван\",\"lastName\":\"Иванов\"}";
	}

	/** Тестовый защищённый маршрут без бизнес-данных приложения. */
	@RestController
	static class ProtectedController {

		/** Отвечает только при действительном Bearer-токене. */
		@GetMapping("/test/protected")
		String data() {
			return "private";
		}
	}
}
