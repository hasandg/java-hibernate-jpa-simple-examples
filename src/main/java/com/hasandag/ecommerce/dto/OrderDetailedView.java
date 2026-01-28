package com.hasandag.ecommerce.dto;

import com.hasandag.ecommerce.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDetailedView extends OrderUpdateDTO {

  private String orderNumber;
  private BigDecimal totalAmount;
  private OrderStatus status;
  private LocalDateTime orderDate;
  private List<OrderItemSummaryDTO> orderItemSummaries;
}
