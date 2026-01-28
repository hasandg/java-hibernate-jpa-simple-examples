package com.hasandag.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressUpdateDTO extends AddressCreateDTO {

  @NotNull(message = "Address ID is required")
  private Long id;
}
