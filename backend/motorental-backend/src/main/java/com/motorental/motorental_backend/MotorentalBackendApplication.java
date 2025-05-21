package com.motorental.motorental_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "MotoRental API", version = "1.0", description = "API para aluguel de motos"))
public class MotorentalBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(MotorentalBackendApplication.class, args);
	}
}
