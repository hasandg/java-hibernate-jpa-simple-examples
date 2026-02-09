package com.hasandag.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductChangeApprovalDTO {

  @NotNull(message = "Moderator ID is required")
  private Long moderatorId;

  @NotNull(message = "Approval decision is required")
  private Boolean approved;

  @Size(max = 500, message = "Comments must not exceed 500 characters")
  private String comments;
}
