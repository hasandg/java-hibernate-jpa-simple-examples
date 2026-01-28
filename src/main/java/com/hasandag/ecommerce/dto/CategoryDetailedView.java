package com.hasandag.ecommerce.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDetailedView extends CategoryUpdateDTO {

  private List<ProductSummaryDTO> products;
}
