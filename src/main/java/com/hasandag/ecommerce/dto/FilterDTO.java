package com.hasandag.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(
    description = "Generic filter criteria for entity search with pagination",
    example =
        """
        {
          "filters": {
            "firstName": "John",
            "lastName": "Doe",
            "email": "john"
          },
          "page": 0,
          "size": 10
        }
        """)
public class FilterDTO {

  // @JsonDeserialize(using = FilterMapDeserializer.class)
  @Schema(
      description =
          "Map of field names to filter values. Supports string (LIKE), number (EQ), range (min/max)",
      example = "{\"firstName\": \"John\", \"minPrice\": 10, \"maxPrice\": 100}")
  private Map<String, Object> filters = new HashMap<>();
  @JsonProperty("page")
  @Schema(description = "Page number (0-indexed)", example = "0", defaultValue = "0")
  private Integer page = 0;
  @JsonProperty("size")
  @Schema(description = "Number of items per page", example = "10", defaultValue = "10")
  private Integer size = 10;

  // Ensure filters map is initialized
  public Map<String, Object> getFilters() {
    if (filters == null) {
      filters = new HashMap<>();
    }
    return filters;
  }

  public void setFilters(Map<String, Object> filters) {
    System.out.println("DEBUG FilterDTO.setFilters called with: " + filters);
    System.out.println("DEBUG FilterDTO.setFilters - filters != null: " + (filters != null));
    if (filters != null) {
      System.out.println("DEBUG FilterDTO.setFilters - filters.size(): " + filters.size());
      System.out.println("DEBUG FilterDTO.setFilters - filters.getClass(): " + filters.getClass());
      this.filters = new HashMap<>(filters);
    } else {
      System.out.println("DEBUG FilterDTO.setFilters - filters is null, creating empty map");
      this.filters = new HashMap<>();
    }
    System.out.println("DEBUG FilterDTO.setFilters - this.filters after set: " + this.filters);
    System.out.println(
        "DEBUG FilterDTO.setFilters - this.filters.size() after set: " + this.filters.size());
  }

  public Object getFilter(String key) {
    return filters != null ? filters.get(key) : null;
  }

  public String getStringFilter(String key) {
    Object value = getFilter(key);
    return value != null ? value.toString().trim() : null;
  }

  public Integer getIntegerFilter(String key) {
    Object value = getFilter(key);
    if (value == null) {
      return null;
    }
    if (value instanceof Integer) {
      return (Integer) value;
    }
    if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    try {
      return Integer.parseInt(value.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public Long getLongFilter(String key) {
    Object value = getFilter(key);
    if (value == null) {
      return null;
    }
    if (value instanceof Long) {
      return (Long) value;
    }
    if (value instanceof Number) {
      return ((Number) value).longValue();
    }
    try {
      return Long.parseLong(value.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public Boolean getBooleanFilter(String key) {
    Object value = getFilter(key);
    if (value == null) {
      return null;
    }
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    return Boolean.parseBoolean(value.toString());
  }

  static class FilterMapDeserializer extends JsonDeserializer<Map<String, Object>> {
    @Override
    @SuppressWarnings("deprecation")
    public Map<String, Object> deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      System.out.println("DEBUG FilterMapDeserializer.deserialize called");
      JsonNode node = p.getCodec().readTree(p);
      System.out.println("DEBUG FilterMapDeserializer - node: " + node);
      System.out.println("DEBUG FilterMapDeserializer - node.isObject(): " + node.isObject());

      Map<String, Object> result = new HashMap<>();
      if (node.isObject()) {
        node.fields()
            .forEachRemaining(
                entry -> {
                  String key = entry.getKey();
                  JsonNode valueNode = entry.getValue();
                  Object value;
                  if (valueNode.isTextual()) {
                    value = valueNode.asText();
                  } else if (valueNode.isNumber()) {
                    if (valueNode.isInt()) {
                      value = valueNode.asInt();
                    } else if (valueNode.isLong()) {
                      value = valueNode.asLong();
                    } else {
                      value = valueNode.asDouble();
                    }
                  } else if (valueNode.isBoolean()) {
                    value = valueNode.asBoolean();
                  } else {
                    value = valueNode.toString();
                  }
                  System.out.println(
                      "DEBUG FilterMapDeserializer - adding key: " + key + ", value: " + value);
                  result.put(key, value);
                });
      }
      System.out.println("DEBUG FilterMapDeserializer - result size: " + result.size());
      System.out.println("DEBUG FilterMapDeserializer - result: " + result);
      return result;
    }
  }
}
