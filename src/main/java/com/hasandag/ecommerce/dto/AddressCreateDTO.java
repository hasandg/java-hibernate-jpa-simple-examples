package com.hasandag.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressCreateDTO {

  @NotBlank(message = "Street is required")
  @Size(max = 255, message = "Street must not exceed 255 characters")
  private String street;

  @NotBlank(message = "City is required")
  @Size(max = 100, message = "City must not exceed 100 characters")
  private String city;

  @NotBlank(message = "State is required")
  @Size(max = 100, message = "State must not exceed 100 characters")
  private String state;

  @NotBlank(message = "Zip code is required")
  @Size(max = 10, message = "Zip code must not exceed 10 characters")
  private String zipCode;

  @NotBlank(message = "Country is required")
  @Size(max = 100, message = "Country must not exceed 100 characters")
  private String country;
}
