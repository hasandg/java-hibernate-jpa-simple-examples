package com.hasandag.ecommerce.controller;

import com.hasandag.ecommerce.dto.ProductChangeRequestResponseDTO;
import com.hasandag.ecommerce.dto.ProductUpdateDTO;
import com.hasandag.ecommerce.service.ProductApprovalService;
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
@Tag(name = "Product Approval", description = "Product change request management APIs")
public class ProductApprovalController {

  private final ProductApprovalService productApprovalService;

  @PostMapping
  @Operation(
      summary = "Submit a product change request",
      description = "Creates a draft change request for a product. Requires moderator approval.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Change request submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Product not found")
      })
  public ResponseEntity<Long> submitChangeRequest(
      @Valid @RequestBody ProductUpdateDTO updateDTO,
      @Parameter(description = "User submitting the request")
          @RequestParam(defaultValue = "anonymous")
          String requestedBy) {
    Long requestId = productApprovalService.createChangeRequest(updateDTO, requestedBy);
    return ResponseEntity.status(HttpStatus.CREATED).body(requestId);
  }

  @GetMapping("/pending")
  @Operation(
      summary = "Get pending change requests",
      description = "Retrieves all product change requests waiting for approval")
  public ResponseEntity<List<ProductChangeRequestResponseDTO>> getPendingRequests() {
    List<ProductChangeRequestResponseDTO> requests = productApprovalService.getPendingRequests();
    return ResponseEntity.ok(requests);
  }

  @PostMapping("/{id}/approve")
  @Operation(
      summary = "Approve a change request",
      description = "Applies the changes to the product and marks request as approved")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Request approved and changes applied"),
        @ApiResponse(responseCode = "404", description = "Request not found"),
        @ApiResponse(responseCode = "400", description = "Request not in pending state")
      })
  public ResponseEntity<Void> approveRequest(
      @Parameter(description = "Change Request ID") @PathVariable Long id) {
    productApprovalService.approveRequest(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/reject")
  @Operation(
      summary = "Reject a change request",
      description = "Marks the change request as rejected without applying changes")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Request rejected"),
        @ApiResponse(responseCode = "404", description = "Request not found"),
        @ApiResponse(responseCode = "400", description = "Request not in pending state")
      })
  public ResponseEntity<Void> rejectRequest(
      @Parameter(description = "Change Request ID") @PathVariable Long id) {
    productApprovalService.rejectRequest(id);
    return ResponseEntity.ok().build();
  }
}
