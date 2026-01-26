package com.hasandag.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerUpdateDTO extends CustomerCreateDTO {

  @NotNull(message = "Customer ID is required")
  private Long id;
}
