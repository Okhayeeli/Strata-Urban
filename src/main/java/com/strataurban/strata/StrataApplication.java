package com.strataurban.strata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = false)
@ConfigurationPropertiesScan
//@OpenAPIDefinition(info = @Info(title = "Strata API", version = "1.0"))
public class StrataApplication {

	public static void main(String[] args) {
		SpringApplication.run(StrataApplication.class, args);
	}

}
