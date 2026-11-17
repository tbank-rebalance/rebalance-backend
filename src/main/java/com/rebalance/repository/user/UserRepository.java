package com.rebalance.repository.user;

import com.rebalance.entity.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Доступ к учётным записям пользователей. */
public interface UserRepository extends JpaRepository<User, Integer> {

	/**
	 * Ищет пользователя по email без учёта регистра.
	 *
	 * @param email адрес для поиска
	 * @return найденный пользователь или пустое значение
	 */
	Optional<User> findByEmailIgnoreCase(String email);

	/**
	 * Проверяет существование email без учёта регистра.
	 *
	 * @param email адрес для проверки
	 * @return {@code true}, если адрес уже зарегистрирован
	 */
	boolean existsByEmailIgnoreCase(String email);
}
