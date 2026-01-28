package com.hasandag.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
    description = "Filter criteria for customer search with pagination",
    example =
        """
        {
          "firstName": "John",
          "lastName": "Doe",
          "email": "john",
          "phone": "555",
          "city": "Istanbul",
          "country": "Turkey",
          "page": 0,
          "size": 10
        }
        """)
public class CustomerFilterDTO {

  @Schema(description = "Filter by first name (partial match, case-insensitive)", example = "John")
  private String firstName;

  @Schema(description = "Filter by last name (partial match, case-insensitive)", example = "Doe")
  private String lastName;

  @Schema(
      description = "Filter by email (partial match, case-insensitive)",
      example = "john.doe@example.com")
  private String email;

  @Schema(description = "Filter by phone number (partial match)", example = "555-1234")
  private String phone;

  @Schema(description = "Filter by city (partial match, case-insensitive)", example = "Istanbul")
  private String city;

  @Schema(description = "Filter by country (partial match, case-insensitive)", example = "Turkey")
  private String country;

  @Schema(description = "Page number (0-indexed)", example = "0", defaultValue = "0")
  private Integer page = 0;

  @Schema(description = "Number of items per page", example = "10", defaultValue = "10")
  private Integer size = 10;
}
