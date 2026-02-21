package com.hasandag.ecommerce.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.hasandag.ecommerce.BaseIntegrationTest;
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
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class OrderFilterIntegrationTest extends BaseIntegrationTest {

  private Long customer1Id;
  private Long customer2Id;
  private Long productId;

  @BeforeEach
  void setUp() throws Exception {
    customer1Id = createCustomer("Alice", "Smith", "alice@test.com", "1111111111");
    customer2Id = createCustomer("Bob", "Jones", "bob@test.com", "2222222222");
    productId = createProduct("Laptop", "Gaming laptop", new BigDecimal("999.99"), 100);

    createOrder(customer1Id, productId, 2, "Handle with care", "FedEx");
    createOrder(customer1Id, productId, 1, "Urgent delivery", "DHL");
    createOrder(customer2Id, productId, 3, "Leave at door", "FedEx");
    createOrder(customer2Id, productId, 1, "Gift wrap please", "UPS");
  }

  @Test
  void shouldFilterWithSingleAndGroup() throws Exception {
    FilterDTO filterDTO = new FilterDTO();
    filterDTO.setFilterGroups(
        List.of(
            new FilterGroup(
                LogicalOperator.AND,
                Map.of("customerId", customer1Id.toString(), "shippingMethod", "FedEx"))));
    filterDTO.setPage(0);
    filterDTO.setSize(10);

    mockMvc
        .perform(
            post("/api/orders/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", is(1)))
        .andExpect(jsonPath("$.content", hasSize(1)))
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
    filterDTO.setPage(0);
    filterDTO.setSize(10);

    mockMvc
        .perform(
            post("/api/orders/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", is(2)))
        .andExpect(jsonPath("$.content", hasSize(2)));
  }

  @Test
  void shouldFilterWithMultipleGroupsCombinedByAnd() throws Exception {
    // Group1 (AND): customerId=customer1Id
    // Group2 (OR): shippingMethod=DHL OR notes="Handle"
    // Combined with AND => customer1's orders that match DHL or "Handle" in notes
    // customer1 has: FedEx/"Handle with care" and DHL/"Urgent delivery"
    // Group2 matches both (FedEx/"Handle with care" matches notes, DHL matches shippingMethod)
    // Combined: customer1 AND (DHL or Handle) => both customer1 orders
    FilterDTO filterDTO = new FilterDTO();
    filterDTO.setGroupOperator(LogicalOperator.AND);
    filterDTO.setFilterGroups(
        List.of(
            new FilterGroup(LogicalOperator.AND, Map.of("customerId", customer1Id.toString())),
            new FilterGroup(
                LogicalOperator.OR, Map.of("shippingMethod", "DHL", "notes", "Handle"))));
    filterDTO.setPage(0);
    filterDTO.setSize(10);

    mockMvc
        .perform(
            post("/api/orders/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", is(2)))
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.content[*].customerId", everyItem(is(customer1Id.intValue()))));
  }

  @Test
  void shouldFilterWithMultipleGroupsCombinedByOr() throws Exception {
    // Group1 (AND): customerId=customer1Id AND shippingMethod=FedEx => 1 order
    // Group2 (AND): customerId=customer2Id AND shippingMethod=UPS => 1 order
    // Combined with OR => 2 orders total
    FilterDTO filterDTO = new FilterDTO();
    filterDTO.setGroupOperator(LogicalOperator.OR);
    filterDTO.setFilterGroups(
        List.of(
            new FilterGroup(
                LogicalOperator.AND,
                Map.of("customerId", customer1Id.toString(), "shippingMethod", "FedEx")),
            new FilterGroup(
                LogicalOperator.AND,
                Map.of("customerId", customer2Id.toString(), "shippingMethod", "UPS"))));
    filterDTO.setPage(0);
    filterDTO.setSize(10);

    mockMvc
        .perform(
            post("/api/orders/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", is(2)))
        .andExpect(jsonPath("$.content", hasSize(2)));
  }

  @Test
  void shouldFilterByNotesLikeSearch() throws Exception {
    FilterDTO filterDTO = new FilterDTO();
    filterDTO.setFilterGroups(
        List.of(new FilterGroup(LogicalOperator.AND, Map.of("notes", "care"))));
    filterDTO.setPage(0);
    filterDTO.setSize(10);

    mockMvc
        .perform(
            post("/api/orders/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", is(1)))
        .andExpect(jsonPath("$.content[0].notes", containsString("care")));
  }

  @Test
  void shouldFilterByShippingMethod() throws Exception {
    FilterDTO filterDTO = new FilterDTO();
    filterDTO.setFilterGroups(
        List.of(new FilterGroup(LogicalOperator.AND, Map.of("shippingMethod", "FedEx"))));
    filterDTO.setPage(0);
    filterDTO.setSize(10);

    mockMvc
        .perform(
            post("/api/orders/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", is(2)))
        .andExpect(jsonPath("$.content[*].shippingMethod", everyItem(containsString("FedEx"))));
  }

  @Test
  void shouldFilterByCustomerId() throws Exception {
    FilterDTO filterDTO = new FilterDTO();
    filterDTO.setFilterGroups(
        List.of(
            new FilterGroup(LogicalOperator.AND, Map.of("customerId", customer2Id.toString()))));
    filterDTO.setPage(0);
    filterDTO.setSize(10);

    mockMvc
        .perform(
            post("/api/orders/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", is(2)))
        .andExpect(jsonPath("$.content[*].customerId", everyItem(is(customer2Id.intValue()))));
  }

  @Test
  void shouldFilterByStatus() throws Exception {
    FilterDTO filterDTO = new FilterDTO();
    filterDTO.setFilterGroups(
        List.of(new FilterGroup(LogicalOperator.AND, Map.of("status", "PENDING"))));
    filterDTO.setPage(0);
    filterDTO.setSize(10);

    mockMvc
        .perform(
            post("/api/orders/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", is(4)))
        .andExpect(jsonPath("$.content[*].status", everyItem(is("PENDING"))));
  }

  @Test
  void shouldFilterByMinTotalAmount() throws Exception {
    // Order with qty=3 => 3 * 999.99 = 2999.97
    FilterDTO filterDTO = new FilterDTO();
    filterDTO.setFilterGroups(
        List.of(new FilterGroup(LogicalOperator.AND, Map.of("minTotalAmount", "2000"))));
    filterDTO.setPage(0);
    filterDTO.setSize(10);

    mockMvc
        .perform(
            post("/api/orders/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", is(1)))
        .andExpect(jsonPath("$.content[0].customerId", is(customer2Id.intValue())));
  }

  @Test
  void shouldSupportBackwardCompatibleFlatFilters() throws Exception {
    FilterDTO filterDTO = new FilterDTO();
    filterDTO.setFilters(Map.of("customerId", customer1Id.toString()));
    filterDTO.setPage(0);
    filterDTO.setSize(10);

    mockMvc
        .perform(
            post("/api/orders/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", is(2)))
        .andExpect(jsonPath("$.content[*].customerId", everyItem(is(customer1Id.intValue()))));
  }

  @Test
  void shouldReturnAllOrdersWhenNoFilters() throws Exception {
    FilterDTO filterDTO = new FilterDTO();
    filterDTO.setPage(0);
    filterDTO.setSize(10);

    mockMvc
        .perform(
            post("/api/orders/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", is(4)))
        .andExpect(jsonPath("$.content", hasSize(4)));
  }

  @Test
  void shouldRespectPagination() throws Exception {
    FilterDTO filterDTO = new FilterDTO();
    filterDTO.setPage(0);
    filterDTO.setSize(2);

    mockMvc
        .perform(
            post("/api/orders/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.totalElements", is(4)))
        .andExpect(jsonPath("$.totalPages", is(2)))
        .andExpect(jsonPath("$.first", is(true)))
        .andExpect(jsonPath("$.last", is(false)));
  }

  @Test
  void shouldReturnEmptyWhenNoMatch() throws Exception {
    FilterDTO filterDTO = new FilterDTO();
    filterDTO.setFilterGroups(
        List.of(new FilterGroup(LogicalOperator.AND, Map.of("shippingMethod", "Pigeon"))));
    filterDTO.setPage(0);
    filterDTO.setSize(10);

    mockMvc
        .perform(
            post("/api/orders/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", is(0)))
        .andExpect(jsonPath("$.content", hasSize(0)));
  }

  private Long createCustomer(String firstName, String lastName, String email, String phone)
      throws Exception {
    CustomerCreateDTO dto = new CustomerCreateDTO();
    dto.setFirstName(firstName);
    dto.setLastName(lastName);
    dto.setEmail(email);
    dto.setPhone(phone);

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

  private Long createProduct(String name, String description, BigDecimal price, int stock)
      throws Exception {
    ProductCreateDTO dto = new ProductCreateDTO();
    dto.setName(name);
    dto.setDescription(description);
    dto.setPrice(price);
    dto.setStock(stock);

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

  private Long createOrder(
      Long customerId, Long prodId, int quantity, String notes, String shippingMethod)
      throws Exception {
    OrderCreateDTO dto = new OrderCreateDTO();
    dto.setCustomerId(customerId);
    dto.setNotes(notes);
    dto.setShippingMethod(shippingMethod);

    OrderItemCreateDTO item = new OrderItemCreateDTO();
    item.setProductId(prodId);
    item.setQuantity(quantity);
    dto.setOrderItems(List.of(item));

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
}
