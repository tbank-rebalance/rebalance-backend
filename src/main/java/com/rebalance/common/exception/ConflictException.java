package com.rebalance.common.exception;

/** Ошибка конфликта с существующими данными*/
public class ConflictException extends RuntimeException {
	public ConflictException(String message) {
		super(message);
	}
}
