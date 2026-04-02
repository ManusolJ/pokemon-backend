package com.poketeambuilder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
@ConfigurationPropertiesScan
public class TeamBuilderApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeamBuilderApplication.class, args);
	}

}
