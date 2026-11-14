package com.rebalance.controller;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.type.LogicalType;

/** Не позволяет преобразовывать числа и логические значения в строковые поля API. */
@Configuration(proxyBeanMethods = false)
public class AuthJsonConfiguration {

	/** Настраивает строгую проверку типов строк при чтении JSON. */
	@Bean
	JsonMapperBuilderCustomizer rejectNonStringValues() {
		return builder -> builder.withCoercionConfig(LogicalType.Textual, coercion -> {
			coercion.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail);
			coercion.setCoercion(CoercionInputShape.Float, CoercionAction.Fail);
			coercion.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
		});
	}
}
