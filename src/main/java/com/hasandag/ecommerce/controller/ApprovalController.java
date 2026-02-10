package com.hasandag.ecommerce.controller;

import com.hasandag.ecommerce.dto.ProductCreateDTO;
import com.hasandag.ecommerce.dto.ProductUpdateDTO;
import com.hasandag.ecommerce.entity.ApprovalRequest;
import com.hasandag.ecommerce.service.GenericApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Tag(name = "Generic Approval", description = "Generic approval system APIs")
public class ApprovalController {

  private final GenericApprovalService approvalService;

  @PostMapping("/product/create")
  @Operation(summary = "Submit product creation request")
  public ResponseEntity<Long> submitProductCreate(
      @Valid @RequestBody ProductCreateDTO createDTO,
      @RequestParam(defaultValue = "anonymous") String requestedBy) {
    Long id = approvalService.submitRequest("PRODUCT", "CREATE", null, createDTO, requestedBy);
    return ResponseEntity.status(HttpStatus.CREATED).body(id);
  }

  @PostMapping("/product/update")
  @Operation(summary = "Submit product update request")
  public ResponseEntity<Long> submitProductUpdate(
      @Valid @RequestBody ProductUpdateDTO updateDTO,
      @RequestParam(defaultValue = "anonymous") String requestedBy) {
    Long id =
        approvalService.submitRequest(
            "PRODUCT", "UPDATE", updateDTO.getId(), updateDTO, requestedBy);
    return ResponseEntity.status(HttpStatus.CREATED).body(id);
  }

  @GetMapping("/pending")
  @Operation(summary = "Get all pending requests")
  public ResponseEntity<List<ApprovalRequest>> getPendingRequests() {
    return ResponseEntity.ok(approvalService.getPendingRequests());
  }

  @PostMapping("/{id}/approve")
  @Operation(summary = "Approve a request")
  public ResponseEntity<Void> approveRequest(@PathVariable Long id) {
    approvalService.approveRequest(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/reject")
  @Operation(summary = "Reject a request")
  public ResponseEntity<Void> rejectRequest(
      @PathVariable Long id, @RequestParam(required = false) String reason) {
    approvalService.rejectRequest(id, reason);
    return ResponseEntity.ok().build();
  }
}
