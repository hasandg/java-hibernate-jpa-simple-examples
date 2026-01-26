package com.hasandag.ecommerce.controller;

import com.hasandag.ecommerce.dto.CustomerCreateDTO;
import com.hasandag.ecommerce.dto.CustomerResponseDTO;
import com.hasandag.ecommerce.dto.CustomerUpdateDTO;
import com.hasandag.ecommerce.dto.DeleteIdsDTO;
import com.hasandag.ecommerce.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer management APIs")
public class CustomerController {

  private final CustomerService customerService;

  @PostMapping
  @Operation(
      summary = "Create a new customer",
      description = "Creates a new customer with the provided details")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Customer created successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or customer email already exists")
      })
  public ResponseEntity<CustomerResponseDTO> create(
      @Valid @RequestBody CustomerCreateDTO createDTO) {
    CustomerResponseDTO response = customerService.create(createDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get customer by ID",
      description = "Retrieves a customer by its unique identifier")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Customer found"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
      })
  public ResponseEntity<CustomerResponseDTO> findById(
      @Parameter(description = "Customer ID", required = true) @PathVariable Long id) {
    CustomerResponseDTO response = customerService.findById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(summary = "Get all customers", description = "Retrieves all customers in the system")
  public ResponseEntity<List<CustomerResponseDTO>> findAll() {
    List<CustomerResponseDTO> responses = customerService.findAll();
    return ResponseEntity.ok(responses);
  }

  @PutMapping
  @Operation(
      summary = "Update customer",
      description =
          "Updates an existing customer with the provided details. Customer ID must be included in the request body.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or customer email already exists")
      })
  public ResponseEntity<CustomerResponseDTO> update(
      @Valid @RequestBody CustomerUpdateDTO updateDTO) {
    CustomerResponseDTO response = customerService.update(updateDTO);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping
  @Operation(
      summary = "Delete customers",
      description = "Deletes one or more customers by their unique identifiers")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Customers deleted successfully"),
        @ApiResponse(responseCode = "404", description = "One or more customers not found")
      })
  public ResponseEntity<Void> delete(@Valid @RequestBody DeleteIdsDTO deleteIdsDTO) {
    customerService.delete(deleteIdsDTO.getIds());
    return ResponseEntity.noContent().build();
  }
}
