package com.hasandag.ecommerce.controller;

import com.hasandag.ecommerce.dto.DeleteIdsDTO;
import com.hasandag.ecommerce.dto.FilterDTO;
import com.hasandag.ecommerce.dto.PageResponseDTO;
import com.hasandag.ecommerce.dto.ProductCreateDTO;
import com.hasandag.ecommerce.dto.ProductDraftResponseDTO;
import com.hasandag.ecommerce.dto.ProductDraftReviewDTO;
import com.hasandag.ecommerce.dto.ProductDraftSubmitDTO;
import com.hasandag.ecommerce.dto.ProductResponseDTO;
import com.hasandag.ecommerce.dto.ProductUpdateDTO;
import com.hasandag.ecommerce.service.ProductService;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product management APIs")
public class ProductController {

  private final ProductService productService;

  @PostMapping
  @Operation(
      summary = "Create a new product",
      description = "Creates a new product with the provided details")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
      })
  public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody ProductCreateDTO createDTO) {
    ProductResponseDTO response = productService.create(createDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get product by ID",
      description = "Retrieves a product by its unique identifier")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found")
      })
  public ResponseEntity<ProductResponseDTO> findById(
      @Parameter(description = "Product ID", required = true) @PathVariable Long id) {
    ProductResponseDTO response = productService.findById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(summary = "Get all products", description = "Retrieves all products in the system")
  public ResponseEntity<List<ProductResponseDTO>> findAll() {
    List<ProductResponseDTO> responses = productService.findAll();
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/category/{categoryId}")
  @Operation(
      summary = "Get products by category",
      description = "Retrieves all products belonging to a specific category")
  public ResponseEntity<List<ProductResponseDTO>> findByCategoryId(
      @Parameter(description = "Category ID", required = true) @PathVariable Long categoryId) {
    List<ProductResponseDTO> responses = productService.findByCategoryId(categoryId);
    return ResponseEntity.ok(responses);
  }

  @PostMapping("/filter")
  @Operation(
      summary = "Filter products with pagination",
      description = "Filters products based on criteria with pagination support")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Products filtered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid filter criteria")
      })
  public ResponseEntity<PageResponseDTO<ProductResponseDTO>> filter(
      @RequestBody FilterDTO filterDTO) {
    PageResponseDTO<ProductResponseDTO> response = productService.filter(filterDTO);
    return ResponseEntity.ok(response);
  }

  @PutMapping
  @Operation(
      summary = "Update product",
      description =
          "Updates an existing product with the provided details. Product ID must be included in the request body.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
      })
  public ResponseEntity<ProductResponseDTO> update(@Valid @RequestBody ProductUpdateDTO updateDTO) {
    ProductResponseDTO response = productService.update(updateDTO);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/drafts")
  @Operation(
      summary = "Submit product update draft",
      description =
          "Submits product changes for moderator approval without applying them immediately")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Draft submitted successfully"),
        @ApiResponse(responseCode = "404", description = "Product or category not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
      })
  public ResponseEntity<ProductDraftResponseDTO> submitDraft(
      @Valid @RequestBody ProductDraftSubmitDTO submitDTO) {
    ProductDraftResponseDTO response = productService.submitDraft(submitDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/drafts/pending")
  @Operation(
      summary = "Get pending product drafts",
      description = "Retrieves all pending product update drafts waiting for moderation")
  public ResponseEntity<List<ProductDraftResponseDTO>> findPendingDrafts() {
    List<ProductDraftResponseDTO> response = productService.findPendingDrafts();
    return ResponseEntity.ok(response);
  }

  @PutMapping("/drafts/{draftId}/approve")
  @Operation(
      summary = "Approve product draft",
      description = "Approves a pending draft and applies its values to the product")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Draft approved successfully"),
        @ApiResponse(responseCode = "404", description = "Draft not found"),
        @ApiResponse(responseCode = "400", description = "Draft is not pending or invalid input")
      })
  public ResponseEntity<ProductResponseDTO> approveDraft(
      @PathVariable Long draftId, @Valid @RequestBody ProductDraftReviewDTO reviewDTO) {
    ProductResponseDTO response =
        productService.approveDraft(draftId, reviewDTO.getModeratorName());
    return ResponseEntity.ok(response);
  }

  @PutMapping("/drafts/{draftId}/reject")
  @Operation(
      summary = "Reject product draft",
      description = "Rejects a pending draft without changing the product")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Draft rejected successfully"),
        @ApiResponse(responseCode = "404", description = "Draft not found"),
        @ApiResponse(responseCode = "400", description = "Draft is not pending or invalid input")
      })
  public ResponseEntity<ProductDraftResponseDTO> rejectDraft(
      @PathVariable Long draftId, @Valid @RequestBody ProductDraftReviewDTO reviewDTO) {
    ProductDraftResponseDTO response =
        productService.rejectDraft(
            draftId, reviewDTO.getModeratorName(), reviewDTO.getRejectionReason());
    return ResponseEntity.ok(response);
  }

  @DeleteMapping
  @Operation(
      summary = "Delete products",
      description = "Deletes one or more products by their unique identifiers")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Products deleted successfully"),
        @ApiResponse(responseCode = "404", description = "One or more products not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
      })
  public ResponseEntity<Void> delete(@Valid @RequestBody DeleteIdsDTO deleteIdsDTO) {
    productService.delete(deleteIdsDTO.getIds());
    return ResponseEntity.noContent().build();
  }
}
