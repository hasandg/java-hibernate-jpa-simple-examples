package com.hasandag.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderUpdateDTO extends OrderCreateDTO {

  @NotNull(message = "Order ID is required")
  private Long id;
}
