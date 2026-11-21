package com.rebalance.common.exception;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-errors")
public class TestErrorController {
    // Ошибка 404
    @GetMapping("/not-found")
    public String notFound() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Объект не найден");
    }

    // Ошибка 409
    @GetMapping("/conflict")
    public String conflict() {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Объект уже существует");
    }

    // Внутренняя ошибка 500
    @GetMapping("/internal")
    public String internal() {
        throw new RuntimeException("Тестовая внутренняя ошибка");
    }
}
