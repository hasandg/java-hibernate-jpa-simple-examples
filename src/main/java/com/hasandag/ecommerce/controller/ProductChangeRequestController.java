package com.hasandag.ecommerce.controller;

import com.hasandag.ecommerce.dto.ProductChangeApprovalDTO;
import com.hasandag.ecommerce.dto.ProductChangeRequestCreateDTO;
import com.hasandag.ecommerce.dto.ProductChangeRequestResponseDTO;
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
@Tag(name = "Product Change Request", description = "Product change approval system APIs")
public class ProductChangeRequestController {

  private final ProductChangeRequestService changeRequestService;

  @PostMapping
  @Operation(
      summary = "Create product change request",
      description =
          "Submit a proposed change to a product. The change will be saved as PENDING and requires moderator approval.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Change request created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Product or User not found"),
        @ApiResponse(
            responseCode = "409",
            description = "A pending change request already exists for this product")
      })
  public ResponseEntity<ProductChangeRequestResponseDTO> createChangeRequest(
      @Valid @RequestBody ProductChangeRequestCreateDTO createDTO) {
    ProductChangeRequestResponseDTO response = changeRequestService.createChangeRequest(createDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get change request by ID",
      description = "Retrieves a specific change request with proposed and current values")
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
      description = "Retrieves all change requests awaiting moderator review (MODERATOR only)")
  public ResponseEntity<List<ProductChangeRequestResponseDTO>> findAllPending() {
    List<ProductChangeRequestResponseDTO> responses = changeRequestService.findAllPending();
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/product/{productId}")
  @Operation(
      summary = "Get change history for a product",
      description = "Retrieves all change requests (pending, approved, rejected) for a product")
  public ResponseEntity<List<ProductChangeRequestResponseDTO>> findByProductId(
      @Parameter(description = "Product ID", required = true) @PathVariable Long productId) {
    List<ProductChangeRequestResponseDTO> responses =
        changeRequestService.findByProductId(productId);
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/user/{userId}")
  @Operation(
      summary = "Get user's change requests",
      description = "Retrieves all change requests submitted by a specific user")
  public ResponseEntity<List<ProductChangeRequestResponseDTO>> findByUserId(
      @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
    List<ProductChangeRequestResponseDTO> responses = changeRequestService.findByUserId(userId);
    return ResponseEntity.ok(responses);
  }

  @PostMapping("/{id}/approve")
  @Operation(
      summary = "Approve change request",
      description =
          "Approve a pending change request and apply changes to the product (MODERATOR only)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Change request approved and applied to product"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input or insufficient privileges"),
        @ApiResponse(responseCode = "404", description = "Change request or moderator not found"),
        @ApiResponse(responseCode = "409", description = "Change request has already been reviewed")
      })
  public ResponseEntity<ProductChangeRequestResponseDTO> approveChangeRequest(
      @Parameter(description = "Change request ID", required = true) @PathVariable Long id,
      @Valid @RequestBody ProductChangeApprovalDTO approvalDTO) {
    ProductChangeRequestResponseDTO response =
        changeRequestService.approveChangeRequest(id, approvalDTO);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{id}/reject")
  @Operation(
      summary = "Reject change request",
      description = "Reject a pending change request. Product remains unchanged (MODERATOR only)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Change request rejected"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input or insufficient privileges"),
        @ApiResponse(responseCode = "404", description = "Change request or moderator not found"),
        @ApiResponse(responseCode = "409", description = "Change request has already been reviewed")
      })
  public ResponseEntity<ProductChangeRequestResponseDTO> rejectChangeRequest(
      @Parameter(description = "Change request ID", required = true) @PathVariable Long id,
      @Valid @RequestBody ProductChangeApprovalDTO approvalDTO) {
    ProductChangeRequestResponseDTO response =
        changeRequestService.rejectChangeRequest(id, approvalDTO);
    return ResponseEntity.ok(response);
  }
}
