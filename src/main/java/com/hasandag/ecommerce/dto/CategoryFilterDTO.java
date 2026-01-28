package com.hasandag.ecommerce.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryFilterDTO {

  private String name;
  private Integer minProductCount;
  private Integer maxProductCount;
  private Integer page = 0;
  private Integer size = 10;
}
