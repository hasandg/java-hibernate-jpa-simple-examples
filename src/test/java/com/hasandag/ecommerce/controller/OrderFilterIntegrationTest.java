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
  private Long product1Id;
  private Long product2Id;
  private Long categoryId;

  @BeforeEach
  void setUp() throws Exception {
    categoryId = createCategory("Electronics", "Electronic devices");

    customer1Id =
        createCustomerWithAddress(
            "Alice", "Smith", "alice@test.com", "1111111111",
            "123 Main St", "New York", "NY", "10001", "USA");
    customer2Id =
        createCustomerWithAddress(
            "Bob", "Jones", "bob@test.com", "2222222222",
            "456 Oak Ave", "London", "ENG", "SW1A 1AA", "UK");

    product1Id = createProduct("Laptop", "Gaming laptop", new BigDecimal("999.99"), 100, categoryId);
    product2Id = createProduct("Mouse", "Wireless mouse", new BigDecimal("29.99"), 200, null);

    createOrder(customer1Id, List.of(item(product1Id, 2), item(product2Id, 1)), "Handle with care", "FedEx");
    createOrder(customer1Id, List.of(item(product1Id, 1)), "Urgent delivery", "DHL");
    createOrder(customer2Id, List.of(item(product1Id, 3)), "Leave at door", "FedEx");
    createOrder(customer2Id, List.of(item(product2Id, 5)), "Gift wrap please", "UPS");
  }

  private org.springframework.test.web.servlet.ResultActions performOrderFilter(
      Map<String, Object> filters) throws Exception {
    return performEntityFilter("/api/orders/filter", filters);
  }

  private org.springframework.test.web.servlet.ResultActions performOrderFilter(FilterDTO filterDTO)
      throws Exception {
    return performEntityFilter("/api/orders/filter", filterDTO);
  }

  private org.springframework.test.web.servlet.ResultActions performCustomerFilter(
      Map<String, Object> filters) throws Exception {
    return performEntityFilter("/api/customers/filter", filters);
  }

  private org.springframework.test.web.servlet.ResultActions performProductFilter(
      Map<String, Object> filters) throws Exception {
    return performEntityFilter("/api/products/filter", filters);
  }

  private org.springframework.test.web.servlet.ResultActions performCategoryFilter(
      Map<String, Object> filters) throws Exception {
    return performEntityFilter("/api/categories/filter", filters);
  }

  private org.springframework.test.web.servlet.ResultActions performEntityFilter(
      String url, Map<String, Object> filters) throws Exception {
    FilterDTO filterDTO = new FilterDTO();
    if (!filters.isEmpty()) {
      filterDTO.setFilterGroups(List.of(new FilterGroup(null, filters)));
    }
    return performEntityFilter(url, filterDTO);
  }

  private org.springframework.test.web.servlet.ResultActions performEntityFilter(
      String url, FilterDTO filterDTO) throws Exception {
    if (filterDTO.getPage() == null) filterDTO.setPage(0);
    if (filterDTO.getSize() == null) filterDTO.setSize(10);
    return mockMvc.perform(
        post(url)
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
      String firstName, String lastName, String email, String phone,
      String street, String city, String state, String zipCode, String country)
      throws Exception {
    CustomerCreateDTO dto = new CustomerCreateDTO();
    dto.setFirstName(firstName);
    dto.setLastName(lastName);
    dto.setEmail(email);
    dto.setPhone(phone);
    dto.setAddress(new AddressCreateDTO(street, city, state, zipCode, country));

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
      String name, String description, BigDecimal price, int stock, Long catId)
      throws Exception {
    ProductCreateDTO dto = new ProductCreateDTO();
    dto.setName(name);
    dto.setDescription(description);
    dto.setPrice(price);
    dto.setStock(stock);
    dto.setCategoryId(catId);

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

  // -- Helper: perform filter on each entity --

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
  class OrderDirectFields {

    @Test
    void shouldFilterByStatus() throws Exception {
      performOrderFilter(Map.of("status", "PENDING"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(4)))
          .andExpect(jsonPath("$.content[*].status", everyItem(is("PENDING"))));
    }

    @Test
    void shouldFilterByStatusCaseInsensitive() throws Exception {
      performOrderFilter(Map.of("status", "pending"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(4)));
    }

    @Test
    void shouldFilterByOrderNumber() throws Exception {
      performOrderFilter(Map.of("orderNumber", "ORD-"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(4)))
          .andExpect(jsonPath("$.content[0].orderNumber", startsWith("ORD-")));
    }
  }

  @Nested
  class OrderRangeFields {

    @Test
    void shouldFilterByMinTotalAmount() throws Exception {
      performOrderFilter(Map.of("minTotalAmount", "2000"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void shouldFilterByMaxTotalAmount() throws Exception {
      performOrderFilter(Map.of("maxTotalAmount", "200"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void shouldFilterByTotalAmountRange() throws Exception {
      performOrderFilter(Map.of("minTotalAmount", "500", "maxTotalAmount", "2100"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void shouldFilterByOrderDateFrom() throws Exception {
      performOrderFilter(Map.of("orderDateFrom", "2020-01-01T00:00:00"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(4)));
    }

    @Test
    void shouldFilterByOrderDateTo() throws Exception {
      performOrderFilter(Map.of("orderDateTo", "2020-01-01T00:00:00"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(0)));
    }
  }

  @Nested
  class OrderRelationIdFields {

    @Test
    void shouldFilterByCustomerId() throws Exception {
      performOrderFilter(Map.of("customerId", customer1Id.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(2)))
          .andExpect(jsonPath("$.content[*].customerId", everyItem(is(customer1Id.intValue()))));
    }
  }

  @Nested
  class OrderDetailJoinFields {

    @Test
    void shouldFilterByNotes() throws Exception {
      performOrderFilter(Map.of("notes", "care"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].notes", containsString("care")));
    }

    @Test
    void shouldFilterByShippingMethod() throws Exception {
      performOrderFilter(Map.of("shippingMethod", "FedEx"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(2)))
          .andExpect(jsonPath("$.content[*].shippingMethod", everyItem(containsString("FedEx"))));
    }
  }

  @Nested
  class OrderItemVerification {

    @Test
    void shouldReturnOrderItemsWithProductDetails() throws Exception {
      performOrderFilter(Map.of("customerId", customer1Id.toString(), "minTotalAmount", "2000"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].orderItems", hasSize(2)))
          .andExpect(jsonPath("$.content[0].orderItems[*].productName", hasItem("Laptop")))
          .andExpect(jsonPath("$.content[0].orderItems[*].productName", hasItem("Mouse")));
    }

    @Test
    void shouldReturnCorrectUnitPriceAndQuantity() throws Exception {
      performOrderFilter(Map.of("customerId", customer2Id.toString(), "maxTotalAmount", "200"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].orderItems[0].quantity", is(5)))
          .andExpect(jsonPath("$.content[0].orderItems[0].unitPrice", is(29.99)));
    }
  }

  // -- Helper: entity creation --

  @Nested
  class GroupedLogic {

    @Test
    void shouldDefaultToAndWhenOperatorOmitted() throws Exception {
      FilterDTO filterDTO = new FilterDTO();
      filterDTO.setFilterGroups(
          List.of(
              new FilterGroup(
                  null, Map.of("customerId", customer1Id.toString(), "shippingMethod", "FedEx"))));

      performOrderFilter(filterDTO)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].customerId", is(customer1Id.intValue())))
          .andExpect(jsonPath("$.content[0].shippingMethod", is("FedEx")));
    }

    @Test
    void shouldFilterWithOrGroup() throws Exception {
      FilterDTO filterDTO = new FilterDTO();
      filterDTO.setFilterGroups(
          List.of(
              new FilterGroup(
                  LogicalOperator.OR, Map.of("shippingMethod", "UPS", "notes", "Urgent"))));

      performOrderFilter(filterDTO)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void shouldCombineMultipleGroupsWithAnd() throws Exception {
      FilterDTO filterDTO = new FilterDTO();
      filterDTO.setFilterGroups(
          List.of(
              new FilterGroup(null, Map.of("customerId", customer1Id.toString())),
              new FilterGroup(
                  LogicalOperator.OR, Map.of("shippingMethod", "DHL", "notes", "Handle"))));

      performOrderFilter(filterDTO)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(2)))
          .andExpect(jsonPath("$.content[*].customerId", everyItem(is(customer1Id.intValue()))));
    }

    @Test
    void shouldCombineMultipleGroupsWithOr() throws Exception {
      FilterDTO filterDTO = new FilterDTO();
      filterDTO.setGroupOperator(LogicalOperator.OR);
      filterDTO.setFilterGroups(
          List.of(
              new FilterGroup(
                  null,
                  Map.of("customerId", customer1Id.toString(), "shippingMethod", "FedEx")),
              new FilterGroup(
                  null,
                  Map.of("customerId", customer2Id.toString(), "shippingMethod", "UPS"))));

      performOrderFilter(filterDTO)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void shouldSupportBackwardCompatibleFlatFilters() throws Exception {
      FilterDTO filterDTO = new FilterDTO();
      filterDTO.setFilters(Map.of("customerId", customer1Id.toString()));

      performOrderFilter(filterDTO)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(2)))
          .andExpect(jsonPath("$.content[*].customerId", everyItem(is(customer1Id.intValue()))));
    }
  }

  @Nested
  class CustomerFilterFields {

    @Test
    void shouldFilterByFirstName() throws Exception {
      performCustomerFilter(Map.of("firstName", "Alice"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].firstName", is("Alice")));
    }

    @Test
    void shouldFilterByLastName() throws Exception {
      performCustomerFilter(Map.of("lastName", "Jones"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].lastName", is("Jones")));
    }

    @Test
    void shouldFilterByEmail() throws Exception {
      performCustomerFilter(Map.of("email", "alice"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].email", containsString("alice")));
    }

    @Test
    void shouldFilterByPhone() throws Exception {
      performCustomerFilter(Map.of("phone", "2222222222"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].firstName", is("Bob")));
    }

    @Test
    void shouldFilterByAddressCity() throws Exception {
      performCustomerFilter(Map.of("city", "London"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].firstName", is("Bob")));
    }

    @Test
    void shouldFilterByAddressCountry() throws Exception {
      performCustomerFilter(Map.of("country", "USA"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].firstName", is("Alice")));
    }

    @Test
    void shouldFilterByAddressState() throws Exception {
      performCustomerFilter(Map.of("state", "NY"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].firstName", is("Alice")));
    }

    @Test
    void shouldFilterByAddressStreet() throws Exception {
      performCustomerFilter(Map.of("street", "Oak"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].firstName", is("Bob")));
    }

    @Test
    void shouldFilterByAddressZipCode() throws Exception {
      performCustomerFilter(Map.of("zipCode", "10001"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].firstName", is("Alice")));
    }
  }

  @Nested
  class ProductFilterFields {

    @Test
    void shouldFilterByProductName() throws Exception {
      performProductFilter(Map.of("name", "Laptop"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].name", containsString("Laptop")));
    }

    @Test
    void shouldFilterByProductDescription() throws Exception {
      performProductFilter(Map.of("description", "Wireless"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].name", is("Mouse")));
    }

    @Test
    void shouldFilterByMinPrice() throws Exception {
      performProductFilter(Map.of("minPrice", "500"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].name", is("Laptop")));
    }

    @Test
    void shouldFilterByMaxPrice() throws Exception {
      performProductFilter(Map.of("maxPrice", "50"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].name", is("Mouse")));
    }

    @Test
    void shouldFilterByPriceRange() throws Exception {
      performProductFilter(Map.of("minPrice", "10", "maxPrice", "100"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].name", is("Mouse")));
    }

    @Test
    void shouldFilterByMinStock() throws Exception {
      performProductFilter(Map.of("minStock", "150"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].name", is("Mouse")));
    }

    @Test
    void shouldFilterByMaxStock() throws Exception {
      performProductFilter(Map.of("maxStock", "95"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].name", is("Laptop")));
    }

    @Test
    void shouldFilterByCategoryId() throws Exception {
      performProductFilter(Map.of("categoryId", categoryId.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].name", is("Laptop")));
    }
  }

  @Nested
  class CategoryFilterFields {

    @Test
    void shouldFilterByCategoryName() throws Exception {
      performCategoryFilter(Map.of("name", "Electronics"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content[0].name", containsString("Electronics")));
    }

    @Test
    void shouldFilterByCategoryDescription() throws Exception {
      performCategoryFilter(Map.of("description", "devices"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void shouldFilterByMinProductCount() throws Exception {
      performCategoryFilter(Map.of("minProductCount", "1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void shouldFilterByMaxProductCount() throws Exception {
      performCategoryFilter(Map.of("maxProductCount", "0"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(0)));
    }
  }

  @Nested
  class PaginationAndEdgeCases {

    @Test
    void shouldReturnAllOrdersWhenNoFilters() throws Exception {
      performOrderFilter(Map.of())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(4)));
    }

    @Test
    void shouldRespectPagination() throws Exception {
      FilterDTO filterDTO = new FilterDTO();
      filterDTO.setPage(0);
      filterDTO.setSize(2);

      performOrderFilter(filterDTO)
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

      performOrderFilter(filterDTO)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(2)))
          .andExpect(jsonPath("$.first", is(false)))
          .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldReturnEmptyWhenNoMatch() throws Exception {
      performOrderFilter(Map.of("shippingMethod", "Pigeon"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    void shouldIgnoreUnknownFilterKeys() throws Exception {
      performOrderFilter(Map.of("nonExistentField", "value"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(4)));
    }

    @Test
    void shouldIgnoreEmptyStringValues() throws Exception {
      performOrderFilter(Map.of("notes", ""))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements", is(4)));
    }
  }
}
