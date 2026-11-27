package com.rebalance.config;

import jakarta.servlet.DispatcherType;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** Настраивает хеширование паролей, подпись и проверку JWT, а также доступ к HTTP-маршрутам. */
@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {
	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfiguration.class);

	/**
	 * Возвращает часы в UTC для установки времени выдачи и окончания действия JWT.
	 *
	 * @return системные часы в UTC
	 */
	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	/**
	 * Создаёт PBKDF2-хешер с идентификатором версии алгоритма в сохранённом хеше.
	 *
	 * @return хешер паролей
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		String id = "pbkdf2@SpringSecurity_v5_8";
		return new DelegatingPasswordEncoder(id, Map.of(id, Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8()));
	}

	/**
	 * Создаёт ключ подписи JWT из Base64-настройки. Временный ключ разрешён только при явной
	 * настройке для локальной разработки.
	 *
	 * @param encodedSecret ключ в Base64 или пустая строка
	 * @param allowEphemeralSecret разрешает временный ключ при отсутствии постоянного
	 * @return ключ для алгоритма HS256
	 * @throws IllegalArgumentException если ключ отсутствует, имеет неверный формат или короче 32 байт
	 */
	@Bean
	SecretKey jwtSecret(
			@Value("${auth.jwt.secret:}") String encodedSecret,
			@Value("${auth.jwt.allow-ephemeral-secret:false}") boolean allowEphemeralSecret) {
		byte[] secret;
		if (encodedSecret.isBlank()) {
			if (!allowEphemeralSecret) {
				throw new IllegalArgumentException("AUTH_JWT_SECRET обязателен для запуска приложения");
			}
			secret = new byte[32];
			new SecureRandom().nextBytes(secret);
			LOGGER.warn("AUTH_JWT_SECRET не задан: временный ключ сменится после перезапуска");
		} else {
			try {
				secret = Base64.getDecoder().decode(encodedSecret);
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException("AUTH_JWT_SECRET должен быть строкой в формате Base64");
			}
			if (secret.length < 32) {
				throw new IllegalArgumentException(
						"AUTH_JWT_SECRET должен содержать не менее 32 байт после Base64-декодирования");
			}
		}
		return new SecretKeySpec(secret, "HmacSHA256");
	}

	/**
	 * Создаёт кодировщик для подписи выдаваемых JWT алгоритмом HS256.
	 *
	 * @param secret ключ подписи
	 * @return кодировщик JWT
	 */
	@Bean
	JwtEncoder jwtEncoder(SecretKey secret) {
		return NimbusJwtEncoder.withSecretKey(secret)
				.algorithm(MacAlgorithm.HS256)
				.build();
	}

	/**
	 * Создаёт декодировщик, который проверяет подпись HS256, срок действия и издателя JWT.
	 *
	 * @param secret ключ проверки подписи
	 * @return декодировщик JWT
	 */
	@Bean
	JwtDecoder jwtDecoder(SecretKey secret) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secret)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("rebalance"));
		return decoder;
	}

	/**
	 * Разрешает публичные маршруты и требует действительный Bearer JWT для остальных запросов.
	 * Серверные HTTP-сессии не создаются.
	 *
	 * @param http объект настройки Spring Security
	 * @return цепочка фильтров безопасности
	 * @throws Exception если Spring Security не может собрать цепочку фильтров
	 */
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.dispatcherTypeMatchers(DispatcherType.ERROR)
						.permitAll()
						.requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login")
						.permitAll()
						.requestMatchers("/actuator/health", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
						.permitAll()
						.anyRequest()
						.authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
		return http.build();
	}
}
