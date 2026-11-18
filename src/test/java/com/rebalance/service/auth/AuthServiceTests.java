package com.rebalance.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.rebalance.dto.request.RegisterRequest;
import com.rebalance.entity.user.User;
import com.rebalance.repository.user.UserRepository;
import java.sql.SQLException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

/** Проверяет преобразование ошибок сохранения пользователя. */
class AuthServiceTests {

	/** Проверяет статус 409 при конфликте уникального email во время сохранения. */
	@Test
	void mapsEmailConstraintViolationToConflict() {
		DataIntegrityViolationException failure = databaseFailure("idx_user_email_lower");
		AuthService service = serviceWithSaveFailure(failure);

		assertThatThrownBy(() -> service.register(request()))
				.isInstanceOfSatisfying(
						ResponseStatusException.class,
						exception ->
								assertThat(exception.getStatusCode().value()).isEqualTo(409));
	}

	/** Проверяет, что другие ошибки БД не маскируются сообщением о занятом email. */
	@Test
	void preservesUnrelatedDatabaseFailure() {
		DataIntegrityViolationException failure = databaseFailure("other_constraint");
		AuthService service = serviceWithSaveFailure(failure);

		assertThatThrownBy(() -> service.register(request())).isSameAs(failure);
	}

	/**
	 * Создаёт сервис, у которого сохранение завершается заданной ошибкой.
	 *
	 * @param failure ошибка сохранения
	 * @return сервис для теста
	 */
	private static AuthService serviceWithSaveFailure(DataIntegrityViolationException failure) {
		UserRepository users = mock(UserRepository.class);
		PasswordEncoder passwords = mock(PasswordEncoder.class);
		when(passwords.encode(anyString())).thenReturn("hash");
		when(users.saveAndFlush(any(User.class))).thenThrow(failure);
		return new AuthService(users, passwords, mock(JwtTokenService.class));
	}

	/**
	 * Создаёт ошибку нарушения ограничения БД.
	 *
	 * @param constraintName имя нарушенного ограничения
	 * @return ошибка доступа к данным с причиной Hibernate
	 */
	private static DataIntegrityViolationException databaseFailure(String constraintName) {
		return new DataIntegrityViolationException(
				"constraint violation",
				new ConstraintViolationException("constraint violation", new SQLException(), constraintName));
	}

	/**
	 * Создаёт корректный запрос регистрации.
	 *
	 * @return запрос регистрации
	 */
	private static RegisterRequest request() {
		return new RegisterRequest("ivan@example.com", "secret123", "Иван", "Иванов");
	}
}
