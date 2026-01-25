package com.hasandag.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Ecommerce API")
                .version("1.0.0")
                .description(
                    "Spring Boot Ecommerce Application REST API\n\n"
                        + "**Entity Relationships:**\n\n"
                        + "1. **Unidirectional OneToOne**: Customer → Address\n"
                        + "2. **Bidirectional OneToOne**: Order ↔ ShippingInfo\n"
                        + "3. **Bidirectional OneToMany**: Order ↔ OrderItem\n"
                        + "4. **Unidirectional OneToMany**: Product → Review\n"
                        + "5. **Bidirectional ManyToMany**: Product ↔ Category\n"
                        + "6. **Unidirectional ManyToMany**: Tag → Product")
                .contact(new Contact().name("API Support").email("support@ecommerce.com"))
                .license(
                    new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
  }

  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("ecommerce-api")
        .pathsToMatch("/api/**")
        .packagesToScan("com.hasandag.ecommerce.controller")
        .build();
  }
}
