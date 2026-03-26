package com.rocket.flinter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class FlinterApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlinterApplication.class, args);
	}

}
