package com.formaprogramada.ecommerce_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaRepositories(basePackages = "com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository")
@EntityScan(basePackages = "com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity")
@SpringBootApplication(
		exclude = {
				org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration.class,
				org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration.class
		}
)
@EnableCaching
@EnableScheduling
public class EcommerceBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(EcommerceBackendApplication.class, args);
	}
}
