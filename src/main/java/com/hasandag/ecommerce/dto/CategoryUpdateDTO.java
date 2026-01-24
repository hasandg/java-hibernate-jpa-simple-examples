package com.hasandag.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateDTO extends CategoryCreateDTO {

  @NotNull(message = "Category ID is required")
  private Long id;
}
