package com.hasandag.ecommerce.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hasandag.ecommerce.BaseIntegrationTest;
import com.hasandag.ecommerce.dto.ProductChangeApprovalDTO;
import com.hasandag.ecommerce.dto.ProductChangeRequestCreateDTO;
import com.hasandag.ecommerce.dto.ProductChangeRequestResponseDTO;
import com.hasandag.ecommerce.dto.ProductCreateDTO;
import com.hasandag.ecommerce.dto.ProductResponseDTO;
import com.hasandag.ecommerce.entity.ProductChangeStatus;
import com.hasandag.ecommerce.entity.User;
import com.hasandag.ecommerce.entity.UserRole;
import com.hasandag.ecommerce.repository.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

@ActiveProfiles("integration-test")
public class ProductChangeRequestControllerTest extends BaseIntegrationTest {

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  private User regularUser;
  private User moderatorUser;
  private Long productId;

  @BeforeEach
  public void setUp() throws Exception {
    // Create test users
    regularUser = new User();
    regularUser.setUsername("regular_user");
    regularUser.setEmail("regular@test.com");
    regularUser.setRole(UserRole.REGULAR_USER);
    regularUser = userRepository.save(regularUser);

    moderatorUser = new User();
    moderatorUser.setUsername("moderator_user");
    moderatorUser.setEmail("moderator@test.com");
    moderatorUser.setRole(UserRole.MODERATOR);
    moderatorUser = userRepository.save(moderatorUser);

    // Create a test product
    ProductCreateDTO productDTO = new ProductCreateDTO();
    productDTO.setName("Test Product");
    productDTO.setDescription("Original description");
    productDTO.setPrice(new BigDecimal("100.00"));
    productDTO.setStock(50);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(productDTO)))
            .andExpect(status().isCreated())
            .andReturn();

    ProductResponseDTO createdProduct =
        objectMapper.readValue(result.getResponse().getContentAsString(), ProductResponseDTO.class);
    productId = createdProduct.getId();
  }

  @Test
  @DisplayName("Should create a change request successfully")
  public void testCreateChangeRequest() throws Exception {
    ProductChangeRequestCreateDTO createDTO = new ProductChangeRequestCreateDTO();
    createDTO.setProductId(productId);
    createDTO.setUserId(regularUser.getId());
    createDTO.setProposedName("Updated Product Name");
    createDTO.setProposedDescription("Updated description");
    createDTO.setProposedPrice(new BigDecimal("150.00"));
    createDTO.setProposedStock(75);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/product-change-requests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.proposedName").value("Updated Product Name"))
            .andExpect(jsonPath("$.proposedPrice").value(150.00))
            .andExpect(jsonPath("$.currentName").value("Test Product"))
            .andExpect(jsonPath("$.currentPrice").value(100.00))
            .andReturn();

    ProductChangeRequestResponseDTO response =
        objectMapper.readValue(
            result.getResponse().getContentAsString(), ProductChangeRequestResponseDTO.class);

    assertThat(response.getId()).isNotNull();
    assertThat(response.getStatus()).isEqualTo(ProductChangeStatus.PENDING);
    assertThat(response.getUserId()).isEqualTo(regularUser.getId());
  }

  @Test
  @DisplayName("Should not allow duplicate pending requests for same product")
  public void testPreventDuplicatePendingRequests() throws Exception {
    // Create first change request
    ProductChangeRequestCreateDTO createDTO = new ProductChangeRequestCreateDTO();
    createDTO.setProductId(productId);
    createDTO.setUserId(regularUser.getId());
    createDTO.setProposedName("First Update");
    createDTO.setProposedDescription("First description");
    createDTO.setProposedPrice(new BigDecimal("150.00"));
    createDTO.setProposedStock(75);

    mockMvc
        .perform(
            post("/api/product-change-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated());

    // Try to create second pending request for same product
    createDTO.setProposedName("Second Update");

    mockMvc
        .perform(
            post("/api/product-change-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("Should list all pending change requests")
  public void testFindAllPending() throws Exception {
    // Create two pending requests with different products
    createChangeRequestForProduct(productId, regularUser.getId());

    // Create another product
    ProductCreateDTO productDTO2 = new ProductCreateDTO();
    productDTO2.setName("Test Product 2");
    productDTO2.setDescription("Description 2");
    productDTO2.setPrice(new BigDecimal("200.00"));
    productDTO2.setStock(100);

    MvcResult productResult =
        mockMvc
            .perform(
                post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(productDTO2)))
            .andExpect(status().isCreated())
            .andReturn();

    ProductResponseDTO product2 =
        objectMapper.readValue(
            productResult.getResponse().getContentAsString(), ProductResponseDTO.class);

    createChangeRequestForProduct(product2.getId(), regularUser.getId());

    // Get all pending requests
    mockMvc
        .perform(get("/api/product-change-requests/pending"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].status").value("PENDING"))
        .andExpect(jsonPath("$[1].status").value("PENDING"));
  }

  @Test
  @DisplayName("Should approve change request and update product")
  public void testApproveChangeRequest() throws Exception {
    // Create change request
    Long requestId = createChangeRequestForProduct(productId, regularUser.getId());

    // Approve the request
    ProductChangeApprovalDTO approvalDTO = new ProductChangeApprovalDTO();
    approvalDTO.setModeratorId(moderatorUser.getId());
    approvalDTO.setApproved(true);
    approvalDTO.setComments("Looks good!");

    mockMvc
        .perform(
            post("/api/product-change-requests/" + requestId + "/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approvalDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVED"))
        .andExpect(jsonPath("$.moderatorId").value(moderatorUser.getId()))
        .andExpect(jsonPath("$.moderatorComments").value("Looks good!"))
        .andExpect(jsonPath("$.reviewedAt").isNotEmpty());

    // Verify product was updated
    MvcResult productResult =
        mockMvc.perform(get("/api/products/" + productId)).andExpect(status().isOk()).andReturn();

    ProductResponseDTO updatedProduct =
        objectMapper.readValue(
            productResult.getResponse().getContentAsString(), ProductResponseDTO.class);

    assertThat(updatedProduct.getName()).isEqualTo("Updated Name");
    assertThat(updatedProduct.getPrice()).isEqualByComparingTo(new BigDecimal("150.00"));
    assertThat(updatedProduct.getStock()).isEqualTo(75);
  }

  @Test
  @DisplayName("Should reject change request and NOT update product")
  public void testRejectChangeRequest() throws Exception {
    // Create change request
    Long requestId = createChangeRequestForProduct(productId, regularUser.getId());

    // Reject the request
    ProductChangeApprovalDTO approvalDTO = new ProductChangeApprovalDTO();
    approvalDTO.setModeratorId(moderatorUser.getId());
    approvalDTO.setApproved(false);
    approvalDTO.setComments("Price increase too high");

    mockMvc
        .perform(
            post("/api/product-change-requests/" + requestId + "/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approvalDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REJECTED"))
        .andExpect(jsonPath("$.moderatorId").value(moderatorUser.getId()))
        .andExpect(jsonPath("$.moderatorComments").value("Price increase too high"));

    // Verify product was NOT updated
    MvcResult productResult =
        mockMvc.perform(get("/api/products/" + productId)).andExpect(status().isOk()).andReturn();

    ProductResponseDTO updatedProduct =
        objectMapper.readValue(
            productResult.getResponse().getContentAsString(), ProductResponseDTO.class);

    assertThat(updatedProduct.getName()).isEqualTo("Test Product");
    assertThat(updatedProduct.getPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
    assertThat(updatedProduct.getStock()).isEqualTo(50);
  }

  @Test
  @DisplayName("Should not allow regular user to approve requests")
  public void testRegularUserCannotApprove() throws Exception {
    Long requestId = createChangeRequestForProduct(productId, regularUser.getId());

    ProductChangeApprovalDTO approvalDTO = new ProductChangeApprovalDTO();
    approvalDTO.setModeratorId(regularUser.getId()); // Regular user as moderator
    approvalDTO.setApproved(true);
    approvalDTO.setComments("Trying to approve");

    mockMvc
        .perform(
            post("/api/product-change-requests/" + requestId + "/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approvalDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should get change history for a product")
  public void testGetProductChangeHistory() throws Exception {
    // Create and approve first request
    Long requestId1 = createChangeRequestForProduct(productId, regularUser.getId());
    approveRequest(requestId1);

    // Create and reject second request (should succeed now that first is approved)
    Long requestId2 = createChangeRequestForProduct(productId, regularUser.getId());
    rejectRequest(requestId2);

    // Get history
    mockMvc
        .perform(get("/api/product-change-requests/product/" + productId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].status").value("APPROVED"))
        .andExpect(jsonPath("$[1].status").value("REJECTED"));
  }

  // Helper methods
  private Long createChangeRequestForProduct(Long productId, Long userId) throws Exception {
    ProductChangeRequestCreateDTO createDTO = new ProductChangeRequestCreateDTO();
    createDTO.setProductId(productId);
    createDTO.setUserId(userId);
    createDTO.setProposedName("Updated Name");
    createDTO.setProposedDescription("Updated description");
    createDTO.setProposedPrice(new BigDecimal("150.00"));
    createDTO.setProposedStock(75);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/product-change-requests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
            .andExpect(status().isCreated())
            .andReturn();

    ProductChangeRequestResponseDTO response =
        objectMapper.readValue(
            result.getResponse().getContentAsString(), ProductChangeRequestResponseDTO.class);
    return response.getId();
  }

  private void approveRequest(Long requestId) throws Exception {
    ProductChangeApprovalDTO approvalDTO = new ProductChangeApprovalDTO();
    approvalDTO.setModeratorId(moderatorUser.getId());
    approvalDTO.setApproved(true);
    approvalDTO.setComments("Approved");

    mockMvc
        .perform(
            post("/api/product-change-requests/" + requestId + "/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approvalDTO)))
        .andExpect(status().isOk());
  }

  private void rejectRequest(Long requestId) throws Exception {
    ProductChangeApprovalDTO approvalDTO = new ProductChangeApprovalDTO();
    approvalDTO.setModeratorId(moderatorUser.getId());
    approvalDTO.setApproved(false);
    approvalDTO.setComments("Rejected");

    mockMvc
        .perform(
            post("/api/product-change-requests/" + requestId + "/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approvalDTO)))
        .andExpect(status().isOk());
  }
}
