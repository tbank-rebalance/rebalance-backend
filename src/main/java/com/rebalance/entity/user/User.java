package com.rebalance.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Учётная запись пользователя приложения. */
@Entity
@Table(name = "\"user\"")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Integer id;

	@Column(nullable = false, length = 255)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(name = "first_name", length = 100)
	private String firstName;

	@Column(name = "last_name", length = 100)
	private String lastName;

	/** Конструктор для восстановления сущности средствами JPA. */
	protected User() {}

	/**
	 * Создаёт пользователя с уже хешированным паролем.
	 *
	 * @param email адрес электронной почты
	 * @param passwordHash хеш пароля
	 * @param firstName имя
	 * @param lastName фамилия
	 */
	public User(String email, String passwordHash, String firstName, String lastName) {
		this.email = email;
		this.passwordHash = passwordHash;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	/**
	 * Возвращает идентификатор пользователя.
	 *
	 * @return идентификатор после сохранения в БД
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Возвращает email пользователя.
	 *
	 * @return адрес электронной почты
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Возвращает сохранённый хеш пароля.
	 *
	 * @return хеш пароля
	 */
	public String getPasswordHash() {
		return passwordHash;
	}

	/**
	 * Возвращает имя пользователя.
	 *
	 * @return имя пользователя
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Возвращает фамилию пользователя.
	 *
	 * @return фамилия пользователя
	 */
	public String getLastName() {
		return lastName;
	}
}
