package com.hasandag.ecommerce.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDetailedView extends CustomerUpdateDTO {

  private AddressSummaryDTO addressSummary;
}
