package com.hasandag.ecommerce.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.hasandag.ecommerce.BaseIntegrationTest;
import com.hasandag.ecommerce.dto.AddressCreateDTO;
import com.hasandag.ecommerce.dto.CategoryCreateDTO;
import com.hasandag.ecommerce.dto.CustomerCreateDTO;
import com.hasandag.ecommerce.dto.FilterDTO;
import com.hasandag.ecommerce.dto.FilterGroup;
import com.hasandag.ecommerce.dto.LogicalOperator;
import com.hasandag.ecommerce.dto.OrderCreateDTO;
import com.hasandag.ecommerce.dto.OrderItemCreateDTO;
import com.hasandag.ecommerce.dto.ProductCreateDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class OrderFilterIntegrationTest extends BaseIntegrationTest {

  private Long customer1Id;
  private Long customer2Id;
  private Long categoryId;

  @BeforeEach
  void setUp() throws Exception {
    categoryId = createCategory("Electronics", "Electronic devices");

    customer1Id =
        createCustomerWithAddress(
            "Alice",
            "Smith",
            "alice@test.com",
            "1111111111",
            "123 Main St",
            "New York",
            "NY",
            "10001",
            "USA");
    customer2Id =
        createCustomerWithAddress(
            "Bob",
            "Jones",
            "bob@test.com",
            "2222222222",
            "456 Oak Ave",
            "London",
            "ENG",
            "SW1A 1AA",
            "UK");

    Long product1Id =
        createProduct("Laptop", "Gaming laptop", new BigDecimal("999.99"), 100, categoryId);
    Long product2Id =
        createProduct("Mouse", "Wireless mouse", new BigDecimal("29.99"), 200, categoryId);

    createOrder(
        customer1Id,
        List.of(item(product1Id, 2), item(product2Id, 1)),
        "Handle with care",
        "FedEx");
    createOrder(customer1Id, List.of(item(product1Id, 1)), "Urgent delivery", "DHL");
    createOrder(customer2Id, List.of(item(product1Id, 3)), "Leave at door", "FedEx");
    createOrder(customer2Id, List.of(item(product2Id, 5)), "Gift wrap please", "UPS");
  }

  private org.springframework.test.web.servlet.ResultActions performFilter(
      Map<String, Object> filters) throws Exception {
    FilterDTO filterDTO = new FilterDTO();
    if (!filters.isEmpty()) {
      filterDTO.setFilterGroups(List.of(new FilterGroup(null, filters)));
    }
    return performFilter(filterDTO);
  }

  private org.springframework.test.web.servlet.ResultActions performFilter(FilterDTO filterDTO)
      throws Exception {
    if (filterDTO.getPage() == null) filterDTO.setPage(0);
    if (filterDTO.getSize() == null) filterDTO.setSize(10);
    return mockMvc.perform(
        post("/api/orders/filter")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(filterDTO)));
  }

  private OrderItemCreateDTO item(Long productId, int quantity) {
    OrderItemCreateDTO dto = new OrderItemCreateDTO();
    dto.setProductId(productId);
    dto.setQuantity(quantity);
    return dto;
  }

  private Long createCustomerWithAddress(
      String firstName,
      String lastName,
      String email,
      String phone,
      String street,
      String city,
      String state,
      String zipCode,
      String country)
      throws Exception {
    CustomerCreateDTO dto = new CustomerCreateDTO();
    dto.setFirstName(firstName);
    dto.setLastName(lastName);
    dto.setEmail(email);
    dto.setPhone(phone);

    AddressCreateDTO address = new AddressCreateDTO(street, city, state, zipCode, country);
    dto.setAddress(address);

    String response =
        mockMvc
            .perform(
                post("/api/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    return ((Number) com.jayway.jsonpath.JsonPath.read(response, "$.id")).longValue();
  }

  private Long createProduct(
      String name, String description, BigDecimal price, int stock, Long categoryId)
      throws Exception {
    ProductCreateDTO dto = new ProductCreateDTO();
    dto.setName(name);
    dto.setDescription(description);
    dto.setPrice(price);
    dto.setStock(stock);
    dto.setCategoryId(categoryId);

    String response =
        mockMvc
            .perform(
                post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    return ((Number) com.jayway.jsonpath.JsonPath.read(response, "$.id")).longValue();
  }

  private Long createCategory(String name, String description) throws Exception {
    CategoryCreateDTO dto = new CategoryCreateDTO();
    dto.setName(name);
    dto.setDescription(description);

    String response =
        mockMvc
            .perform(
                post("/api/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    return ((Number) com.jayway.jsonpath.JsonPath.read(response, "$.id")).longValue();
  }

  private Long createOrder(
      Long customerId, List<OrderItemCreateDTO> items, String notes, String shippingMethod)
      throws Exception {
    OrderCreateDTO dto = new OrderCreateDTO();
    dto.setCustomerId(customerId);
    dto.setNotes(notes);
    dto.setShippingMethod(shippingMethod);
    dto.setOrderItems(items);

    String response =
        mockMvc
            .perform(
                post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    return ((Number) com.jayway.jsonpath.JsonPath.read(response, "$.id")).longValue();
  }

  @Nested
  class DirectFieldFilters {

    @Test
    void shouldFilterByOrderNumber() throws Exception {
      performFilter(Map.of("status", "PENDING"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].orderNumber", startsWith("ORD-")));
    }

    @Test
    void shouldFilterByStatus() throws Exception {
      performFilter(Map.of("status", "PENDING"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(4)))
          .andExpect(jsonPath("$.content[*].status", everyItem(is("PENDING"))));
    }

    @Test
    void shouldFilterByStatusCaseInsensitive() throws Exception {
      performFilter(Map.of("status", "pending"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(4)));
    }
  }

  @Nested
  class RangeFilters {

    @Test
    void shouldFilterByMinTotalAmount() throws Exception {
      performFilter(Map.of("minTotalAmount", "2000"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void shouldFilterByMaxTotalAmount() throws Exception {
      performFilter(Map.of("maxTotalAmount", "200"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].customerId", is(customer2Id.intValue())));
    }

    @Test
    void shouldFilterByAmountRange() throws Exception {
      FilterDTO filterDTO = new FilterDTO();
      filterDTO.setFilterGroups(
          List.of(
              new FilterGroup(null, Map.of("minTotalAmount", "500", "maxTotalAmount", "2100"))));

      performFilter(filterDTO)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void shouldFilterByOrderDateFrom() throws Exception {
      performFilter(Map.of("orderDateFrom", "2020-01-01T00:00:00"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(4)));
    }

    @Test
    void shouldFilterByOrderDateTo() throws Exception {
      performFilter(Map.of("orderDateTo", "2020-01-01T00:00:00"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(0)));
    }
  }

  @Nested
  class RelationIdFilters {

    @Test
    void shouldFilterByCustomerId() throws Exception {
      performFilter(Map.of("customerId", customer2Id.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(2)))
          .andExpect(jsonPath("$.content[*].customerId", everyItem(is(customer2Id.intValue()))));
    }
  }

  @Nested
  class JoinedEntityFilters {

    @Test
    void shouldFilterByNotes() throws Exception {
      performFilter(Map.of("notes", "care"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].notes", containsString("care")));
    }

    @Test
    void shouldFilterByShippingMethod() throws Exception {
      performFilter(Map.of("shippingMethod", "FedEx"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(2)))
          .andExpect(jsonPath("$.content[*].shippingMethod", everyItem(containsString("FedEx"))));
    }
  }

  @Nested
  class MultiItemOrders {

    @Test
    void shouldCalculateTotalForMultipleItems() throws Exception {
      performFilter(Map.of("customerId", customer1Id.toString(), "minTotalAmount", "2000"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].orderItems", hasSize(2)));
    }

    @Test
    void shouldReturnOrderItemDetails() throws Exception {
      performFilter(Map.of("customerId", customer2Id.toString(), "maxTotalAmount", "200"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].orderItems[0].quantity", is(5)));
    }
  }

  @Nested
  class GroupedFilters {

    @Test
    void shouldFilterWithSingleAndGroupDefaultOperator() throws Exception {
      FilterDTO filterDTO = new FilterDTO();
      filterDTO.setFilterGroups(
          List.of(
              new FilterGroup(
                  null, Map.of("customerId", customer1Id.toString(), "shippingMethod", "FedEx"))));

      performFilter(filterDTO)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].customerId", is(customer1Id.intValue())))
          .andExpect(jsonPath("$.content[0].shippingMethod", is("FedEx")));
    }

    @Test
    void shouldFilterWithSingleOrGroup() throws Exception {
      FilterDTO filterDTO = new FilterDTO();
      filterDTO.setFilterGroups(
          List.of(
              new FilterGroup(
                  LogicalOperator.OR, Map.of("shippingMethod", "UPS", "notes", "Urgent"))));

      performFilter(filterDTO)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void shouldFilterWithMultipleGroupsCombinedByAnd() throws Exception {
      FilterDTO filterDTO = new FilterDTO();
      filterDTO.setFilterGroups(
          List.of(
              new FilterGroup(null, Map.of("customerId", customer1Id.toString())),
              new FilterGroup(
                  LogicalOperator.OR, Map.of("shippingMethod", "DHL", "notes", "Handle"))));

      performFilter(filterDTO)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(2)))
          .andExpect(jsonPath("$.content[*].customerId", everyItem(is(customer1Id.intValue()))));
    }

    @Test
    void shouldFilterWithMultipleGroupsCombinedByOr() throws Exception {
      FilterDTO filterDTO = new FilterDTO();
      filterDTO.setGroupOperator(LogicalOperator.OR);
      filterDTO.setFilterGroups(
          List.of(
              new FilterGroup(
                  null, Map.of("customerId", customer1Id.toString(), "shippingMethod", "FedEx")),
              new FilterGroup(
                  null, Map.of("customerId", customer2Id.toString(), "shippingMethod", "UPS"))));

      performFilter(filterDTO)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(2)));
    }
  }

  @Nested
  class BackwardCompatibility {

    @Test
    void shouldSupportFlatFilters() throws Exception {
      FilterDTO filterDTO = new FilterDTO();
      filterDTO.setFilters(Map.of("customerId", customer1Id.toString()));

      performFilter(filterDTO)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(2)))
          .andExpect(jsonPath("$.content[*].customerId", everyItem(is(customer1Id.intValue()))));
    }
  }

  @Nested
  class PaginationAndEdgeCases {

    @Test
    void shouldReturnAllOrdersWhenNoFilters() throws Exception {
      performFilter(Map.of())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(4)));
    }

    @Test
    void shouldRespectPagination() throws Exception {
      FilterDTO filterDTO = new FilterDTO();
      filterDTO.setPage(0);
      filterDTO.setSize(2);

      performFilter(filterDTO)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(2)))
          .andExpect(jsonPath("$.totalElements", is(4)))
          .andExpect(jsonPath("$.totalPages", is(2)))
          .andExpect(jsonPath("$.first", is(true)))
          .andExpect(jsonPath("$.last", is(false)));
    }

    @Test
    void shouldReturnSecondPage() throws Exception {
      FilterDTO filterDTO = new FilterDTO();
      filterDTO.setPage(1);
      filterDTO.setSize(2);

      performFilter(filterDTO)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(2)))
          .andExpect(jsonPath("$.first", is(false)))
          .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldReturnEmptyWhenNoMatch() throws Exception {
      performFilter(Map.of("shippingMethod", "Pigeon"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(0)))
          .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void shouldIgnoreUnknownFilterKeys() throws Exception {
      performFilter(Map.of("nonExistentField", "value"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(4)));
    }

    @Test
    void shouldIgnoreEmptyStringValues() throws Exception {
      performFilter(Map.of("notes", ""))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(4)));
    }
  }
}
