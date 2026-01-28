package com.hasandag.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemUpdateDTO extends OrderItemCreateDTO {

  @NotNull(message = "Order Item ID is required")
  private Long id;
}
