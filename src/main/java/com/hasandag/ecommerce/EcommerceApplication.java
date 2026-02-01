package com.hasandag.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.hasandag.ecommerce")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(
    basePackages = "com.hasandag.ecommerce.repository")
public class EcommerceApplication {
  public static void main(String[] args) {
    SpringApplication.run(EcommerceApplication.class, args);
  }
}
