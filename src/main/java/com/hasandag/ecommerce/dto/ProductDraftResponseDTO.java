package com.hasandag.ecommerce.dto;

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
public class ProductDraftResponseDTO {

  private Long id;
  private Long productId;
  private String name;
  private String description;
  private BigDecimal price;
  private Integer stock;
  private Long categoryId;
  private String categoryName;
  private String status;
  private String submittedBy;
  private LocalDateTime submittedAt;
  private String reviewedBy;
  private LocalDateTime reviewedAt;
  private String rejectionReason;
}
