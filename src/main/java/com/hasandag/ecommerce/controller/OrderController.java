package com.hasandag.ecommerce.controller;

import com.hasandag.ecommerce.dto.DeleteIdsDTO;
import com.hasandag.ecommerce.dto.OrderCreateDTO;
import com.hasandag.ecommerce.dto.OrderResponseDTO;
import com.hasandag.ecommerce.dto.OrderUpdateDTO;
import com.hasandag.ecommerce.service.OrderService;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order management APIs")
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  @Operation(
      summary = "Create a new order",
      description = "Creates a new order with order items and calculates total amount")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data or insufficient stock")
      })
  public ResponseEntity<OrderResponseDTO> create(@Valid @RequestBody OrderCreateDTO createDTO) {
    OrderResponseDTO response = orderService.create(createDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get order by ID",
      description = "Retrieves an order by its unique identifier")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found")
      })
  public ResponseEntity<OrderResponseDTO> findById(
      @Parameter(description = "Order ID", required = true) @PathVariable Long id) {
    OrderResponseDTO response = orderService.findById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(summary = "Get all orders", description = "Retrieves all orders in the system")
  public ResponseEntity<List<OrderResponseDTO>> findAll() {
    List<OrderResponseDTO> responses = orderService.findAll();
    return ResponseEntity.ok(responses);
  }

  @PutMapping
  @Operation(
      summary = "Update order",
      description =
          "Updates an existing order with the provided details. Order ID must be included in the request body.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Order updated successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or order cannot be updated")
      })
  public ResponseEntity<OrderResponseDTO> update(@Valid @RequestBody OrderUpdateDTO updateDTO) {
    OrderResponseDTO response = orderService.update(updateDTO);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping
  @Operation(
      summary = "Delete orders",
      description =
          "Deletes one or more orders by their unique identifiers and restores product stock")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Orders deleted successfully"),
        @ApiResponse(responseCode = "404", description = "One or more orders not found"),
        @ApiResponse(responseCode = "400", description = "One or more orders cannot be deleted")
      })
  public ResponseEntity<Void> delete(@Valid @RequestBody DeleteIdsDTO deleteIdsDTO) {
    orderService.delete(deleteIdsDTO.getIds());
    return ResponseEntity.noContent().build();
  }
}
