package com.hasandag.ecommerce.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDTO {

  private Long id;
  private Integer quantity;
  private BigDecimal unitPrice;
  private Long productId;
  private String productName;
}
