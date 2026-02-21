package com.hasandag.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(
    description = "Generic filter criteria with grouped AND/OR logic and pagination",
    example =
        """
        {
          "filterGroups": [
            {
              "operator": "AND",
              "filters": { "customerId": "10", "shippingMethod": "Fly" }
            },
            {
              "operator": "OR",
              "filters": { "notes": "great", "customerId": "20" }
            }
          ],
          "groupOperator": "AND",
          "page": 0,
          "size": 10
        }
        """)
public class FilterDTO {

  @Schema(
      description =
          "List of filter groups. Each group has its own operator (AND/OR) and a set of field-value filters.")
  @NotNull
  @Valid
  private List<FilterGroup> filterGroups = new ArrayList<>();

  @Schema(
      description = "Logical operator to combine filter groups together",
      example = "AND",
      defaultValue = "AND")
  @NotNull
  private LogicalOperator groupOperator = LogicalOperator.AND;

  @Schema(
      description =
          "Flat filters map (backward compatible). If provided without filterGroups, treated as a single AND group.",
      example = "{\"firstName\": \"John\", \"lastName\": \"Doe\"}")
  private Map<String, Object> filters;

  @Schema(description = "Page number (0-indexed)", example = "0", defaultValue = "0")
  @NotNull
  @Min(0)
  private Integer page = 0;

  @Schema(description = "Number of items per page", example = "10", defaultValue = "10")
  @NotNull
  @Min(1)
  @Max(100)
  private Integer size = 10;

  public List<FilterGroup> getResolvedFilterGroups() {
    if (!filterGroups.isEmpty()) {
      return filterGroups;
    }
    if (filters != null && !filters.isEmpty()) {
      return List.of(new FilterGroup(LogicalOperator.AND, new HashMap<>(filters)));
    }
    return List.of();
  }

  public Object getFilter(String key) {
    for (FilterGroup group : getResolvedFilterGroups()) {
      Object value = group.getFilters().get(key);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  public String getStringFilter(String key) {
    Object value = getFilter(key);
    return value != null ? value.toString().trim() : null;
  }

  public Integer getIntegerFilter(String key) {
    Object value = getFilter(key);
    if (value == null) return null;
    if (value instanceof Integer i) return i;
    if (value instanceof Number n) return n.intValue();
    try {
      return Integer.parseInt(value.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public Long getLongFilter(String key) {
    Object value = getFilter(key);
    if (value == null) return null;
    if (value instanceof Long l) return l;
    if (value instanceof Number n) return n.longValue();
    try {
      return Long.parseLong(value.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public Boolean getBooleanFilter(String key) {
    Object value = getFilter(key);
    if (value == null) return null;
    if (value instanceof Boolean b) return b;
    return Boolean.parseBoolean(value.toString());
  }
}
