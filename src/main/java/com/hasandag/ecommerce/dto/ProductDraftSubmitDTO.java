package com.hasandag.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDraftSubmitDTO extends ProductUpdateDTO {

  @NotBlank(message = "Submitted by is required")
  private String submittedBy;
}
