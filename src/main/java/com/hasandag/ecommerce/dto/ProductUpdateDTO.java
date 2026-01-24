package com.hasandag.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductUpdateDTO extends ProductCreateDTO {

  @NotNull(message = "Product ID is required")
  private Long id;
}
