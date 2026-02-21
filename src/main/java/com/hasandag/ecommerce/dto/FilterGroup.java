package com.hasandag.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A group of filters combined by a logical operator (AND/OR)")
public class FilterGroup {

  @Schema(
      description = "Logical operator to combine filters within this group",
      example = "AND",
      defaultValue = "AND")
  @NotNull
  private LogicalOperator operator = LogicalOperator.AND;

  @Schema(
      description = "Map of field names to filter values within this group",
      example = "{\"customerId\": \"10\", \"shippingMethod\": \"Fly\"}")
  @NotNull
  private Map<String, Object> filters = new HashMap<>();
}
