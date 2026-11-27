package com.rebalance.common.exception;

/** Ошибка временной недоступности внешнего сервиса */
public class ExternalServiceUnavailableException extends RuntimeException {
	public ExternalServiceUnavailableException() {
		super("Внешний сервис временно недоступен");
	}

	public ExternalServiceUnavailableException(Throwable cause) {
		super("Внешний сервис временно недоступен", cause);
	}
}
