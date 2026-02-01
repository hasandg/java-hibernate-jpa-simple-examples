package com.hasandag.ecommerce.controller;

import com.hasandag.ecommerce.dto.CustomerCreateDTO;
import com.hasandag.ecommerce.dto.CustomerResponseDTO;
import com.hasandag.ecommerce.dto.CustomerUpdateDTO;
import com.hasandag.ecommerce.dto.DeleteIdsDTO;
import com.hasandag.ecommerce.dto.FilterDTO;
import com.hasandag.ecommerce.dto.PageResponseDTO;
import com.hasandag.ecommerce.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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

  @PostMapping("/filter")
  @Operation(
      summary = "Filter customers with pagination",
      description =
          "Filters customers based on criteria with pagination support. All filter fields are optional and support partial matching (case-insensitive for text fields).\n\n"
              + "**Example Use Cases:**\n\n"
              + "1. **Search by name**: Filter customers by first or last name\n"
              + "2. **Search by email**: Find customers with specific email domain\n"
              + "3. **Search by location**: Filter by city or country\n"
              + "4. **Combined filters**: Use multiple criteria together\n\n"
              + "**Examples:**\n\n"
              + "```json\n"
              + "// Search by first name\n"
              + "{\n"
              + "  \"firstName\": \"John\",\n"
              + "  \"page\": 0,\n"
              + "  \"size\": 10\n"
              + "}\n"
              + "```\n\n"
              + "```json\n"
              + "// Search by city and country\n"
              + "{\n"
              + "  \"city\": \"Istanbul\",\n"
              + "  \"country\": \"Turkey\",\n"
              + "  \"page\": 0,\n"
              + "  \"size\": 20\n"
              + "}\n"
              + "```\n\n"
              + "```json\n"
              + "// Search by email domain\n"
              + "{\n"
              + "  \"email\": \"@gmail.com\",\n"
              + "  \"page\": 0,\n"
              + "  \"size\": 10\n"
              + "}\n"
              + "```\n\n"
              + "```json\n"
              + "// Combined search\n"
              + "{\n"
              + "  \"firstName\": \"John\",\n"
              + "  \"lastName\": \"Doe\",\n"
              + "  \"city\": \"Istanbul\",\n"
              + "  \"page\": 0,\n"
              + "  \"size\": 10\n"
              + "}\n"
              + "```")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Customers filtered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid filter criteria")
      })
  @RequestBody(
      description = "Filter criteria for customer search",
      required = true,
      content =
          @Content(
              mediaType = "application/json",
              examples = {
                @ExampleObject(
                    name = "Search by name",
                    summary = "Filter by first name",
                    value = "{\"filters\": {\"firstName\": \"John\"}, \"page\": 0, \"size\": 10}"),
                @ExampleObject(
                    name = "Search by location",
                    summary = "Filter by city and country",
                    value =
                        "{\"filters\": {\"city\": \"Istanbul\", \"country\": \"Turkey\"}, \"page\": 0, \"size\": 10}"),
                @ExampleObject(
                    name = "Search by email",
                    summary = "Filter by email domain",
                    value =
                        "{\"filters\": {\"email\": \"@gmail.com\"}, \"page\": 0, \"size\": 10}"),
                @ExampleObject(
                    name = "Combined search",
                    summary = "Multiple filter criteria",
                    value =
                        "{\"filters\": {\"firstName\": \"John\", \"lastName\": \"Doe\", \"city\": \"Istanbul\"}, \"page\": 0, \"size\": 10}"),
                @ExampleObject(
                    name = "Empty filter",
                    summary = "Get all customers with pagination",
                    value = "{\"filters\": {}, \"page\": 0, \"size\": 10}")
              }))
  public ResponseEntity<PageResponseDTO<CustomerResponseDTO>> filter(
      @RequestBody FilterDTO filterDTO) {
    System.out.println("DEBUG Controller: Received filterDTO = " + filterDTO);
    System.out.println("DEBUG Controller: filterDTO != null: " + (filterDTO != null));
    System.out.println("DEBUG TESTING FILTER DTO");
    if (filterDTO != null) {
      System.out.println("DEBUG Controller: filterDTO.getFilters() = " + filterDTO.getFilters());
      System.out.println(
          "DEBUG Controller: filterDTO.getFilters() != null: " + (filterDTO.getFilters() != null));
      System.out.println(
          "DEBUG Controller: filterDTO.getFilters().size() = "
              + (filterDTO.getFilters() != null ? filterDTO.getFilters().size() : "null"));
      if (filterDTO.getFilters() != null && !filterDTO.getFilters().isEmpty()) {
        System.out.println(
            "DEBUG Controller: filterDTO.getFilters().keySet() = "
                + filterDTO.getFilters().keySet());
        for (Map.Entry<String, Object> entry : filterDTO.getFilters().entrySet()) {
          System.out.println(
              "DEBUG Controller: filter[" + entry.getKey() + "] = " + entry.getValue());
        }
      }
    }
    PageResponseDTO<CustomerResponseDTO> response = customerService.filter(filterDTO);
    return ResponseEntity.ok(response);
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
