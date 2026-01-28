package com.hasandag.ecommerce.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDetailedView extends OrderItemUpdateDTO {

  private BigDecimal unitPrice;
  private ProductSummaryDTO product;
}
