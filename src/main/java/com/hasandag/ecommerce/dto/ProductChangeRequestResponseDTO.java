package com.hasandag.ecommerce.dto;

import com.hasandag.ecommerce.entity.ProductChangeStatus;
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
  private Long productId;
  private String productName; // Current product name for comparison
  private Long userId;
  private String username;

  // Proposed changes
  private String proposedName;
  private String proposedDescription;
  private BigDecimal proposedPrice;
  private Integer proposedStock;

  // Current values for comparison
  private String currentName;
  private String currentDescription;
  private BigDecimal currentPrice;
  private Integer currentStock;

  // Status tracking
  private ProductChangeStatus status;
  private Long moderatorId;
  private String moderatorUsername;
  private String moderatorComments;
  private LocalDateTime reviewedAt;

  // Timestamps
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
