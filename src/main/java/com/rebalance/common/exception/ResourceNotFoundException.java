package com.rebalance.common.exception;

/** Ошибка отсутствия запрашиваемого объекта */
public class ResourceNotFoundException extends RuntimeException {
	public ResourceNotFoundException(String message) {
		super(message);
	}
}
