package com.rebalance.service.auth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtEncoder;

/** Проверяет ограничения настройки срока действия JWT. */
class JwtTokenServiceTests {

	/** Проверяет отказ при нулевом или отрицательном сроке действия. */
	@Test
	void rejectsNonPositiveLifetime() {
		JwtEncoder encoder = mock(JwtEncoder.class);

		assertThatThrownBy(() -> new JwtTokenService(encoder, Clock.systemUTC(), Duration.ZERO))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("положительным");
		assertThatThrownBy(() -> new JwtTokenService(encoder, Clock.systemUTC(), Duration.ofMinutes(-1)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("положительным");
	}
}
