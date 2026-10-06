package com.tuon.tbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;

/**
 * Starts the TBank Spring Boot application.
 */
@SpringBootApplication
public class TbankApplication {

	public static void main(String[] args) {
		SpringApplication.run(TbankApplication.class, args);
	}

}
