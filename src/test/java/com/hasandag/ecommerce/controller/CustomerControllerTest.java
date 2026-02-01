package com.hasandag.ecommerce.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.hasandag.ecommerce.BaseIntegrationTest;
import com.hasandag.ecommerce.dto.AddressCreateDTO;
import com.hasandag.ecommerce.dto.CustomerCreateDTO;
import com.hasandag.ecommerce.dto.CustomerUpdateDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class CustomerControllerTest extends BaseIntegrationTest {

  @Test
  void shouldCreateCustomer() throws Exception {
    CustomerCreateDTO createDTO = new CustomerCreateDTO();
    createDTO.setFirstName("John");
    createDTO.setLastName("Doe");
    createDTO.setEmail("john.doe@example.com");
    createDTO.setPhone("1234567890");

    AddressCreateDTO address = new AddressCreateDTO();
    address.setStreet("123 Main St");
    address.setCity("New York");
    address.setState("NY");
    address.setZipCode("10001");
    address.setCountry("USA");
    createDTO.setAddress(address);

    mockMvc
        .perform(
            post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.email", is("john.doe@example.com")));
  }

  @Test
  void shouldGetAllCustomers() throws Exception {
    // Create a customer first
    shouldCreateCustomer();

    mockMvc
        .perform(get("/api/customers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(greaterThan(0))));
  }

  @Test
  void shouldGetCustomerById() throws Exception {
    // 1. Create a customer
    CustomerCreateDTO createDTO = new CustomerCreateDTO();
    createDTO.setFirstName("Jane");
    createDTO.setLastName("Doe");
    createDTO.setEmail("jane.doe@example.com");
    createDTO.setPhone("0987654321");

    String response =
        mockMvc
            .perform(
                post("/api/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    Integer id = com.jayway.jsonpath.JsonPath.read(response, "$.id");

    // 2. Get the customer
    mockMvc
        .perform(get("/api/customers/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email", is("jane.doe@example.com")));
  }

  @Test
  void shouldUpdateCustomer() throws Exception {
    // 1. Create a customer
    CustomerCreateDTO createDTO = new CustomerCreateDTO();
    createDTO.setFirstName("Update");
    createDTO.setLastName("Me");
    createDTO.setEmail("update.me@example.com");
    createDTO.setPhone("1112223333");

    String response =
        mockMvc
            .perform(
                post("/api/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    Integer id = com.jayway.jsonpath.JsonPath.read(response, "$.id");

    // 2. Update the customer
    CustomerUpdateDTO updateDTO = new CustomerUpdateDTO();
    updateDTO.setId(id.longValue());
    updateDTO.setFirstName("Updated");
    updateDTO.setLastName("User");
    updateDTO.setEmail("updated.user@example.com"); // Changed email
    updateDTO.setPhone("1112223333");

    mockMvc
        .perform(
            put("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName", is("Updated")))
        .andExpect(jsonPath("$.email", is("updated.user@example.com")));
  }

  @Test
  void shouldDeleteCustomer() throws Exception {
    // 1. Create a customer
    CustomerCreateDTO createDTO = new CustomerCreateDTO();
    createDTO.setFirstName("Delete");
    createDTO.setLastName("Me");
    createDTO.setEmail("delete.me@example.com");
    createDTO.setPhone("4445556666");

    String response =
        mockMvc
            .perform(
                post("/api/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    Integer id = com.jayway.jsonpath.JsonPath.read(response, "$.id");

    // 2. Delete the customer using DeleteIdsDTO
    String deletePayload = "{\"ids\": [" + id + "]}";
    mockMvc
        .perform(
            delete("/api/customers").contentType(MediaType.APPLICATION_JSON).content(deletePayload))
        .andExpect(status().isNoContent());

    // 3. Verify deletion
    mockMvc.perform(get("/api/customers/" + id)).andExpect(status().isNotFound());
  }

  @Test
  void shouldReturnNotFoundForNonExistentCustomer() throws Exception {
    mockMvc.perform(get("/api/customers/999999")).andExpect(status().isNotFound());
  }
}
