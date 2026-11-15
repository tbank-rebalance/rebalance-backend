package com.rebalance.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Общие сведения об API приложения в Swagger UI. */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

	/** Создаёт описание API для OpenAPI-документа. */
	@Bean
	OpenAPI rebalanceOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Ребаланс API")
						.version("1.0")
						.description("API приложения для учёта личного бюджета"));
	}
}
