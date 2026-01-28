package com.hasandag.ecommerce.dto;

import com.hasandag.ecommerce.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderFilterDTO {

  private String orderNumber;
  private OrderStatus status;
  private BigDecimal minTotalAmount;
  private BigDecimal maxTotalAmount;
  private LocalDateTime orderDateFrom;
  private LocalDateTime orderDateTo;
  private Integer page = 0;
  private Integer size = 10;
}
