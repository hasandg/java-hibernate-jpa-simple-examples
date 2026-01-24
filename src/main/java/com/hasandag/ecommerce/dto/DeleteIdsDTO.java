package com.hasandag.ecommerce.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteIdsDTO {

  @NotEmpty(message = "At least one ID is required")
  private List<Long> ids;
}
