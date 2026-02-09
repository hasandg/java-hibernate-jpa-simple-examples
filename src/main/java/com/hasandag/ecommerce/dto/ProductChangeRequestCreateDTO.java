package com.hasandag.ecommerce.dto;

import com.hasandag.ecommerce.entity.ChangeRequestType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductChangeRequestCreateDTO {

  @NotNull(message = "Change request type is required")
  private ChangeRequestType type;

  private Long productId;

  @NotBlank(message = "Product name is required")
  @Size(max = 255, message = "Product name must not exceed 255 characters")
  private String name;

  @NotBlank(message = "Product description is required")
  @Size(max = 1000, message = "Product description must not exceed 1000 characters")
  private String description;

  @NotNull(message = "Product price is required")
  @DecimalMin(value = "0.01", message = "Price must be greater than 0")
  @Digits(integer = 8, fraction = 2, message = "Invalid price format")
  private BigDecimal price;

  @NotNull(message = "Product stock is required")
  @Min(value = 0, message = "Stock must be non-negative")
  private Integer stock;

  private Long categoryId;

  @NotBlank(message = "Requester identifier is required")
  private String requestedBy;
}
