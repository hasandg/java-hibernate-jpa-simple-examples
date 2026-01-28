package com.hasandag.ecommerce.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDetailedView extends ProductUpdateDTO {

  private CategorySummaryDTO category;
}
