package com.hasandag.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateDTO {

  @NotBlank(message = "Category name is required")
  @Size(max = 255, message = "Category name must not exceed 255 characters")
  private String name;

  @Size(max = 500, message = "Category description must not exceed 500 characters")
  private String description;
}
