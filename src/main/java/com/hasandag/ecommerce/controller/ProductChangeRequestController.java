package com.hasandag.ecommerce.controller;

import com.hasandag.ecommerce.dto.ChangeRequestReviewDTO;
import com.hasandag.ecommerce.dto.ProductChangeRequestCreateDTO;
import com.hasandag.ecommerce.dto.ProductChangeRequestResponseDTO;
import com.hasandag.ecommerce.entity.ChangeRequestStatus;
import com.hasandag.ecommerce.service.ProductChangeRequestService;
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
@RequestMapping("/api/product-change-requests")
@RequiredArgsConstructor
@Tag(name = "Product Change Request", description = "Product approval workflow APIs")
public class ProductChangeRequestController {

  private final ProductChangeRequestService changeRequestService;

  @PostMapping
  @Operation(
      summary = "Submit a product change request",
      description = "Submits a new or updated product as a draft pending moderator approval")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Change request submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Referenced product or category not found")
      })
  public ResponseEntity<ProductChangeRequestResponseDTO> submit(
      @Valid @RequestBody ProductChangeRequestCreateDTO createDTO) {
    ProductChangeRequestResponseDTO response = changeRequestService.submitChangeRequest(createDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get change request by ID",
      description = "Retrieves a specific change request by its unique identifier")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Change request found"),
        @ApiResponse(responseCode = "404", description = "Change request not found")
      })
  public ResponseEntity<ProductChangeRequestResponseDTO> findById(
      @Parameter(description = "Change request ID", required = true) @PathVariable Long id) {
    ProductChangeRequestResponseDTO response = changeRequestService.findById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/pending")
  @Operation(
      summary = "Get all pending change requests",
      description = "Retrieves all change requests awaiting moderator review")
  public ResponseEntity<List<ProductChangeRequestResponseDTO>> findPending() {
    List<ProductChangeRequestResponseDTO> responses = changeRequestService.findPending();
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/status/{status}")
  @Operation(
      summary = "Get change requests by status",
      description = "Retrieves change requests filtered by their approval status")
  public ResponseEntity<List<ProductChangeRequestResponseDTO>> findByStatus(
      @Parameter(description = "Change request status", required = true) @PathVariable
          ChangeRequestStatus status) {
    List<ProductChangeRequestResponseDTO> responses = changeRequestService.findByStatus(status);
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/product/{productId}")
  @Operation(
      summary = "Get change requests for a product",
      description = "Retrieves all change requests associated with a specific product")
  public ResponseEntity<List<ProductChangeRequestResponseDTO>> findByProductId(
      @Parameter(description = "Product ID", required = true) @PathVariable Long productId) {
    List<ProductChangeRequestResponseDTO> responses =
        changeRequestService.findByProductId(productId);
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/requester/{requestedBy}")
  @Operation(
      summary = "Get change requests by requester",
      description = "Retrieves all change requests submitted by a specific user")
  public ResponseEntity<List<ProductChangeRequestResponseDTO>> findByRequestedBy(
      @Parameter(description = "Requester identifier", required = true) @PathVariable
          String requestedBy) {
    List<ProductChangeRequestResponseDTO> responses =
        changeRequestService.findByRequestedBy(requestedBy);
    return ResponseEntity.ok(responses);
  }

  @PutMapping("/{id}/review")
  @Operation(
      summary = "Review a change request",
      description =
          "Moderator approves or rejects a pending change request. "
              + "On approval, changes are applied to the product entity")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Change request reviewed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid review data or already reviewed"),
        @ApiResponse(responseCode = "404", description = "Change request not found")
      })
  public ResponseEntity<ProductChangeRequestResponseDTO> review(
      @Parameter(description = "Change request ID", required = true) @PathVariable Long id,
      @Valid @RequestBody ChangeRequestReviewDTO reviewDTO) {
    ProductChangeRequestResponseDTO response = changeRequestService.review(id, reviewDTO);
    return ResponseEntity.ok(response);
  }
}
