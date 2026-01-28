package com.hasandag.ecommerce.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductFilterDTO {

  private String name;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private Integer minStock;
  private Integer maxStock;
  private Long categoryId;
  private Integer page = 0;
  private Integer size = 10;
}
