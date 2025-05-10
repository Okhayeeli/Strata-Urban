package com.strataurban.strata;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = false)

//@OpenAPIDefinition(info = @Info(title = "Strata API", version = "1.0"))
public class StrataApplication {

	public static void main(String[] args) {
		SpringApplication.run(StrataApplication.class, args);
	}

}
