package com.hasandag.ecommerce.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

  private Long id;
  private String name;
  private String description;
  private BigDecimal price;
  private Integer stock;
  private Long categoryId;
  private String categoryName;
}
