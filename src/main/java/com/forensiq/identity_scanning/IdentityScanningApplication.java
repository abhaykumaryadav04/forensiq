package com.forensiq.identity_scanning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = {"http://localhost:5173/"})
@SpringBootApplication
public class IdentityScanningApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdentityScanningApplication.class, args);
	}

}
