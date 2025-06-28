package com.formaprogramada.ecommerce_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
@EnableJpaRepositories(basePackages = "com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository")
@EntityScan(basePackages = "com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity")
@SpringBootApplication
public class EcommerceBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(EcommerceBackendApplication.class, args);
	}
}
