package com.strataurban.strata;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@OpenAPIDefinition(info = @Info(title = "Strata API", version = "1.0"))
public class StrataApplication {

	public static void main(String[] args) {
		SpringApplication.run(StrataApplication.class, args);
	}

}
