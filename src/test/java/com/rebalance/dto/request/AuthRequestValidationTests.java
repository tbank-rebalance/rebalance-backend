package com.rebalance.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Проверки граничных значений запросов регистрации и входа. */
class AuthRequestValidationTests {

	private static ValidatorFactory validatorFactory;
	private static Validator validator;

	@BeforeAll
	static void setUp() {
		validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();
	}

	@AfterAll
	static void tearDown() {
		validatorFactory.close();
	}

	@Test
	void acceptsRegistrationPasswordAtBothLengthLimits() {
		assertThat(validator.validate(new RegisterRequest("ivan@example.com", "secret12", "Иван", "Иванов")))
				.isEmpty();
		assertThat(validator.validate(new RegisterRequest("ivan@example.com", "a1" + "x".repeat(62), "Иван", "Иванов")))
				.isEmpty();
	}

	@Test
	void rejectsMissingAndInvalidRegistrationFields() {
		assertThat(invalidFields(new RegisterRequest(null, "12345678", " ", "Иванов")))
				.containsExactlyInAnyOrder("email", "password", "firstName");
		assertThat(invalidFields(new RegisterRequest("invalid", "abcdefgh", "Иван", "x".repeat(101))))
				.containsExactlyInAnyOrder("email", "password", "lastName");
		assertThat(invalidFields(new RegisterRequest("ivan@example.com", "a1" + "x".repeat(63), "Иван", "Иванов")))
				.containsExactly("password");
	}

	@Test
	void loginChecksPresenceAndEmailButNotPasswordStrength() {
		assertThat(validator.validate(new LoginRequest("ivan@example.com", "x")))
				.isEmpty();
		assertThat(invalidFields(new LoginRequest("invalid", " "))).containsExactlyInAnyOrder("email", "password");
	}

	private static Set<String> invalidFields(Object request) {
		return validator.validate(request).stream()
				.map(violation -> violation.getPropertyPath().toString())
				.collect(Collectors.toSet());
	}
}
