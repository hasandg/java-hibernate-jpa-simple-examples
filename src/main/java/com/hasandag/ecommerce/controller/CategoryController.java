package com.hasandag.ecommerce.controller;

import com.hasandag.ecommerce.dto.CategoryCreateDTO;
import com.hasandag.ecommerce.dto.CategoryFilterDTO;
import com.hasandag.ecommerce.dto.CategoryResponseDTO;
import com.hasandag.ecommerce.dto.CategoryUpdateDTO;
import com.hasandag.ecommerce.dto.DeleteIdsDTO;
import com.hasandag.ecommerce.dto.PageResponseDTO;
import com.hasandag.ecommerce.service.CategoryService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Category management APIs")
public class CategoryController {

  private final CategoryService categoryService;

  @PostMapping
  @Operation(
      summary = "Create a new category",
      description = "Creates a new category with the provided details")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Category created successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or category name already exists")
      })
  public ResponseEntity<CategoryResponseDTO> create(
      @Valid @RequestBody CategoryCreateDTO createDTO) {
    CategoryResponseDTO response = categoryService.create(createDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get category by ID",
      description = "Retrieves a category by its unique identifier")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Category found"),
        @ApiResponse(responseCode = "404", description = "Category not found")
      })
  public ResponseEntity<CategoryResponseDTO> findById(
      @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
    CategoryResponseDTO response = categoryService.findById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(summary = "Get all categories", description = "Retrieves all categories in the system")
  public ResponseEntity<List<CategoryResponseDTO>> findAll() {
    List<CategoryResponseDTO> responses = categoryService.findAll();
    return ResponseEntity.ok(responses);
  }

  @PostMapping("/filter")
  @Operation(
      summary = "Filter categories with pagination",
      description = "Filters categories based on criteria with pagination support")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Categories filtered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid filter criteria")
      })
  public ResponseEntity<PageResponseDTO<CategoryResponseDTO>> filter(
      @Valid @RequestBody CategoryFilterDTO filterDTO) {
    PageResponseDTO<CategoryResponseDTO> response = categoryService.filter(filterDTO);
    return ResponseEntity.ok(response);
  }

  @PutMapping
  @Operation(
      summary = "Update category",
      description =
          "Updates an existing category with the provided details. Category ID must be included in the request body.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or category name already exists")
      })
  public ResponseEntity<CategoryResponseDTO> update(
      @Valid @RequestBody CategoryUpdateDTO updateDTO) {
    CategoryResponseDTO response = categoryService.update(updateDTO);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping
  @Operation(
      summary = "Delete categories",
      description = "Deletes one or more categories by their unique identifiers")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Categories deleted successfully"),
        @ApiResponse(responseCode = "404", description = "One or more categories not found"),
        @ApiResponse(
            responseCode = "400",
            description = "One or more categories have associated products")
      })
  public ResponseEntity<Void> delete(@Valid @RequestBody DeleteIdsDTO deleteIdsDTO) {
    categoryService.delete(deleteIdsDTO.getIds());
    return ResponseEntity.noContent().build();
  }
}
