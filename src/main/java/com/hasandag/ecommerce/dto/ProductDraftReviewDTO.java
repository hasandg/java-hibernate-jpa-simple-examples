package com.hasandag.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDraftReviewDTO {

  @NotBlank(message = "Moderator name is required")
  private String moderatorName;

  @Size(max = 1000, message = "Rejection reason must not exceed 1000 characters")
  private String rejectionReason;
}
