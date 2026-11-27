package com.rebalance.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Контроллер с тестовыми эндпоинтами для проверки обработки ошибок API. */
@RestController
@RequestMapping("/test-errors")
public class TestErrorController {
	// Проверка обработки ошибки 404 через ResponseStatusException
	@GetMapping("/not-found")
	public String notFound() {
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Объект не найден");
	}

	// Проверка обработки ошибки 409 через ResponseStatusException
	@GetMapping("/conflict")
	public String conflict() {
		throw new ResponseStatusException(HttpStatus.CONFLICT, "Объект уже существует");
	}

	// Проверка обработки непредвиденной внутренней ошибки 500
	@GetMapping("/internal")
	public String internal() {
		throw new RuntimeException("Тестовая внутренняя ошибка");
	}

	// Проверка обработки ошибки 405 при обращении методом GET вместо POST
	@PostMapping("/post-only")
	public String postOnly() {
		return "OK";
	}

	// Проверка обработки ошибки 400 при отправке некорректного JSON
	@PostMapping("/json-test")
	public String jsonTest(@RequestBody Object body) {
		return "OK";
	}

	// Проверка обработки ошибки 401 при отсутствии действительной авторизации
	@GetMapping("/unauthorized")
	public String unauthorized() {
		throw new UnauthorizedException();
	}

	// Проверка обработки ошибки 404 при отсутствии запрашиваемого объекта
	@GetMapping("/resource-not-found")
	public String resourceNotFound() {
		throw new ResourceNotFoundException("Запрашиваемый объект не найден");
	}

	// Проверка обработки ошибки 409 при конфликте данных
	@GetMapping("/business-conflict")
	public String businessConflict() {
		throw new ConflictException("Объект уже существует");
	}

	// Проверка обработки ошибки 503 при недоступности внешнего сервиса
	@GetMapping("/service-unavailable")
	public String serviceUnavailable() {
		throw new ExternalServiceUnavailableException();
	}
}
