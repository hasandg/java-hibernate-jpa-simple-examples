package com.hasandag.ecommerce.dto;

import com.hasandag.ecommerce.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

  private Long id;
  private String orderNumber;
  private BigDecimal totalAmount;
  private OrderStatus status;
  private LocalDateTime orderDate;
  private List<OrderItemResponseDTO> orderItems;
  private String notes;
  private String shippingMethod;
}
