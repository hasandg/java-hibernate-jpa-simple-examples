package com.hasandag.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRequestReviewDTO {

  @NotNull(message = "Approval decision is required")
  private Boolean approved;

  private String rejectionReason;

  @NotBlank(message = "Reviewer identifier is required")
  private String reviewedBy;
}
