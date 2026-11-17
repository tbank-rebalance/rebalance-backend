package com.rebalance.service.auth;

import com.rebalance.entity.user.User;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Выдаёт подписанные JWT для зарегистрированных пользователей. */
@Service
public class JwtTokenService {

	private final JwtEncoder encoder;
	private final Clock clock;
	private final Duration lifetime;

	/**
	 * Создаёт сервис выдачи JWT с заданным временем жизни.
	 *
	 * @param encoder кодировщик, подписывающий JWT
	 * @param clock часы для времени выдачи и окончания действия
	 * @param lifetime срок действия токена; по умолчанию один час
	 * @throws IllegalArgumentException если срок действия не положителен
	 */
	public JwtTokenService(JwtEncoder encoder, Clock clock, @Value("${auth.jwt.lifetime:PT1H}") Duration lifetime) {
		if (lifetime.isZero() || lifetime.isNegative()) {
			throw new IllegalArgumentException("AUTH_JWT_LIFETIME должен быть положительным");
		}
		this.encoder = encoder;
		this.clock = clock;
		this.lifetime = lifetime;
	}

	/**
	 * Выдаёт подписанный JWT с идентификатором пользователя в поле subject.
	 *
	 * @param user пользователь, прошедший проверку учётных данных
	 * @return подписанный JWT
	 */
	public String issue(User user) {
		Instant now = clock.instant();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("rebalance")
				.subject(user.getId().toString())
				.issuedAt(now)
				.expiresAt(now.plus(lifetime))
				.id(UUID.randomUUID().toString())
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}
}
