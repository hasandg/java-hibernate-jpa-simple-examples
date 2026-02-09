package com.hasandag.ecommerce.dto;

import com.hasandag.ecommerce.entity.ChangeRequestStatus;
import com.hasandag.ecommerce.entity.ChangeRequestType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductChangeRequestResponseDTO {

  private Long id;
  private ChangeRequestType type;
  private ChangeRequestStatus status;
  private Long productId;

  private String proposedName;
  private String proposedDescription;
  private BigDecimal proposedPrice;
  private Integer proposedStock;
  private Long proposedCategoryId;
  private String proposedCategoryName;

  private String requestedBy;
  private String reviewedBy;
  private String rejectionReason;
  private LocalDateTime requestedAt;
  private LocalDateTime reviewedAt;

  private ProductResponseDTO originalProduct;
}
