package com.hasandag.ecommerce.dto;

import com.hasandag.ecommerce.entity.ChangeRequestStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductChangeRequestResponseDTO {
  private Long id;
  private Long productId;
  private String productName; // Current name
  private String proposedName;
  private String proposedDescription;
  private BigDecimal proposedPrice;
  private Integer proposedStock;
  private Long proposedCategoryId;
  private ChangeRequestStatus status;
  private LocalDateTime requestDate;
  private String requestedBy;
}
