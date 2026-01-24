package com.hasandag.ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {

  @NotEmpty(message = "Order items are required")
  @Valid
  private List<OrderItemCreateDTO> orderItems;

  private String notes;

  private String shippingMethod;
}
