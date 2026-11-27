package com.rebalance.service.auth;

import com.rebalance.dto.request.LoginRequest;
import com.rebalance.dto.request.RegisterRequest;
import com.rebalance.dto.response.LoginResponse;
import com.rebalance.dto.response.RegisterResponse;
import com.rebalance.entity.user.User;
import com.rebalance.repository.user.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Регистрирует пользователей и проверяет учётные данные при входе. */
@Service
public class AuthService {

	private final UserRepository users;
	private final PasswordEncoder passwords;
	private final JwtTokenService tokens;
	private final String dummyPasswordHash;

	/**
	 * Создаёт сервис регистрации и входа.
	 *
	 * @param users хранилище пользователей
	 * @param passwords хешер и проверяющий паролей
	 * @param tokens сервис выдачи JWT
	 */
	public AuthService(UserRepository users, PasswordEncoder passwords, JwtTokenService tokens) {
		this.users = users;
		this.passwords = passwords;
		this.tokens = tokens;
		this.dummyPasswordHash = passwords.encode("dummy-password-never-used");
	}

	/**
	 * Регистрирует пользователя с уникальным без учёта регистра email и сохраняет хеш пароля.
	 *
	 * @param request регистрационные данные
	 * @return публичные данные созданного пользователя
	 * @throws ResponseStatusException если email уже занят; статус 409
	 */
	@Transactional
	public RegisterResponse register(RegisterRequest request) {
		if (users.existsByEmailIgnoreCase(request.email())) {
			throw emailAlreadyExists();
		}
		User user = new User(
				request.email(), passwords.encode(request.password()), request.firstName(), request.lastName());
		try {
			User saved = users.saveAndFlush(user);
			return new RegisterResponse(saved.getId(), saved.getEmail(), saved.getFirstName(), saved.getLastName());
		} catch (DataIntegrityViolationException exception) {
			if (isEmailUniquenessViolation(exception)) {
				throw emailAlreadyExists();
			}
			throw exception;
		}
	}

	/**
	 * Проверяет учётные данные и выдаёт JWT при успешном входе.
	 *
	 * @param request email и пароль
	 * @return токен доступа с типом Bearer
	 * @throws ResponseStatusException если учётные данные неверны; статус 401
	 */
	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		var user = users.findByEmailIgnoreCase(request.email());
		String hash = user.map(User::getPasswordHash).orElse(dummyPasswordHash);
		if (!passwords.matches(request.password(), hash) || user.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный email или пароль");
		}
		return new LoginResponse(tokens.issue(user.orElseThrow()), "Bearer");
	}

	/**
	 * Создаёт ошибку для повторной регистрации email.
	 *
	 * @return исключение со статусом 409
	 */
	private static ResponseStatusException emailAlreadyExists() {
		return new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь с таким email уже зарегистрирован");
	}

	/**
	 * Проверяет, что ошибка БД относится именно к индексу уникальности email.
	 *
	 * @param exception ошибка сохранения пользователя
	 * @return {@code true} при нарушении уникальности email
	 */
	private static boolean isEmailUniquenessViolation(DataIntegrityViolationException exception) {
		for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
			if (cause instanceof ConstraintViolationException violation
					&& "idx_user_email_lower".equals(violation.getConstraintName())) {
				return true;
			}
		}
		return false;
	}
}
