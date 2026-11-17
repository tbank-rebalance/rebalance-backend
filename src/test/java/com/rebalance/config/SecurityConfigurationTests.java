package com.rebalance.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;
import org.junit.jupiter.api.Test;

/** Проверяет настройку ключа подписи JWT. */
class SecurityConfigurationTests {

	private final SecurityConfiguration configuration = new SecurityConfiguration();

	/** Проверяет принятие ключа минимальной допустимой длины. */
	@Test
	void acceptsBase64KeyWithAtLeastThirtyTwoBytes() {
		byte[] key = new byte[32];
		String encoded = Base64.getEncoder().encodeToString(key);

		assertThat(configuration.jwtSecret(encoded).getEncoded()).containsExactly(key);
	}

	/** Проверяет понятную ошибку без раскрытия неверного значения ключа. */
	@Test
	void explainsInvalidBase64WithoutExposingItsValue() {
		assertThatThrownBy(() -> configuration.jwtSecret("invalid-secret!"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("AUTH_JWT_SECRET должен быть строкой в формате Base64");
	}

	/** Проверяет отказ при ключе короче 32 байт. */
	@Test
	void rejectsShortKey() {
		String encoded = Base64.getEncoder().encodeToString(new byte[31]);

		assertThatThrownBy(() -> configuration.jwtSecret(encoded))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("не менее 32 байт");
	}
}
