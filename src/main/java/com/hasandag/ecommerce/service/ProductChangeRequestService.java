package com.hasandag.ecommerce.service;

import com.hasandag.ecommerce.dto.ProductChangeApprovalDTO;
import com.hasandag.ecommerce.dto.ProductChangeRequestCreateDTO;
import com.hasandag.ecommerce.dto.ProductChangeRequestResponseDTO;
import com.hasandag.ecommerce.entity.Product;
import com.hasandag.ecommerce.entity.ProductChangeRequest;
import com.hasandag.ecommerce.entity.ProductChangeStatus;
import com.hasandag.ecommerce.entity.User;
import com.hasandag.ecommerce.entity.UserRole;
import com.hasandag.ecommerce.mapper.ProductChangeRequestMapper;
import com.hasandag.ecommerce.repository.ProductChangeRequestRepository;
import com.hasandag.ecommerce.repository.ProductRepository;
import com.hasandag.ecommerce.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductChangeRequestService {

  private final ProductChangeRequestRepository changeRequestRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final ProductChangeRequestMapper mapper;

  @Transactional
  public ProductChangeRequestResponseDTO createChangeRequest(
      ProductChangeRequestCreateDTO createDTO) {

    // Validate product exists
    Product product =
        productRepository
            .findById(createDTO.getProductId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Product not found with id: " + createDTO.getProductId()));

    // Validate user exists
    User user =
        userRepository
            .findById(createDTO.getUserId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "User not found with id: " + createDTO.getUserId()));

    // Validate user has appropriate role
    if (user.getRole() != UserRole.REGULAR_USER
        && user.getRole() != UserRole.MODERATOR
        && user.getRole() != UserRole.ADMIN) {
      throw new IllegalArgumentException("User does not have permission to create change requests");
    }

    // Check for existing pending requests for this product
    List<ProductChangeRequest> pendingRequests =
        changeRequestRepository.findByProductIdAndStatus(
            createDTO.getProductId(), ProductChangeStatus.PENDING);
    if (!pendingRequests.isEmpty()) {
      throw new IllegalStateException(
          "A pending change request already exists for this product. "
              + "Please wait for it to be reviewed before submitting another.");
    }

    // Create the change request
    ProductChangeRequest changeRequest = mapper.toEntity(createDTO);
    changeRequest.setProduct(product);
    changeRequest.setUser(user);
    changeRequest.setStatus(ProductChangeStatus.PENDING);

    ProductChangeRequest saved = changeRequestRepository.save(changeRequest);
    return mapper.toResponseDTO(saved);
  }

  @Transactional(readOnly = true)
  public ProductChangeRequestResponseDTO findById(Long id) {
    ProductChangeRequest changeRequest =
        changeRequestRepository
            .findById(id)
            .orElseThrow(
                () -> new EntityNotFoundException("Change request not found with id: " + id));
    return mapper.toResponseDTO(changeRequest);
  }

  @Transactional(readOnly = true)
  public List<ProductChangeRequestResponseDTO> findAllPending() {
    return changeRequestRepository.findByStatus(ProductChangeStatus.PENDING).stream()
        .map(mapper::toResponseDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ProductChangeRequestResponseDTO> findByProductId(Long productId) {
    return changeRequestRepository.findByProductId(productId).stream()
        .map(mapper::toResponseDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ProductChangeRequestResponseDTO> findByUserId(Long userId) {
    return changeRequestRepository.findByUserId(userId).stream()
        .map(mapper::toResponseDTO)
        .toList();
  }

  @Transactional
  public ProductChangeRequestResponseDTO approveChangeRequest(
      Long requestId, ProductChangeApprovalDTO approvalDTO) {

    // Validate moderator exists and has appropriate role
    User moderator =
        userRepository
            .findById(approvalDTO.getModeratorId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Moderator not found with id: " + approvalDTO.getModeratorId()));

    if (moderator.getRole() != UserRole.MODERATOR && moderator.getRole() != UserRole.ADMIN) {
      throw new IllegalArgumentException("User does not have moderator privileges");
    }

    // Get the change request
    ProductChangeRequest changeRequest =
        changeRequestRepository
            .findById(requestId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Change request not found with id: " + requestId));

    // Validate it's still pending
    if (changeRequest.getStatus() != ProductChangeStatus.PENDING) {
      throw new IllegalStateException(
          "Change request has already been reviewed with status: " + changeRequest.getStatus());
    }

    // Apply changes to the product
    Product product = changeRequest.getProduct();
    if (changeRequest.getProposedName() != null) {
      product.setName(changeRequest.getProposedName());
    }
    if (changeRequest.getProposedDescription() != null) {
      product.setDescription(changeRequest.getProposedDescription());
    }
    if (changeRequest.getProposedPrice() != null) {
      product.setPrice(changeRequest.getProposedPrice());
    }
    if (changeRequest.getProposedStock() != null) {
      product.setStock(changeRequest.getProposedStock());
    }
    productRepository.save(product);

    // Update the change request status
    changeRequest.setStatus(ProductChangeStatus.APPROVED);
    changeRequest.setModerator(moderator);
    changeRequest.setModeratorComments(approvalDTO.getComments());
    changeRequest.setReviewedAt(LocalDateTime.now());

    ProductChangeRequest updated = changeRequestRepository.save(changeRequest);
    return mapper.toResponseDTO(updated);
  }

  @Transactional
  public ProductChangeRequestResponseDTO rejectChangeRequest(
      Long requestId, ProductChangeApprovalDTO approvalDTO) {

    // Validate moderator exists and has appropriate role
    User moderator =
        userRepository
            .findById(approvalDTO.getModeratorId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Moderator not found with id: " + approvalDTO.getModeratorId()));

    if (moderator.getRole() != UserRole.MODERATOR && moderator.getRole() != UserRole.ADMIN) {
      throw new IllegalArgumentException("User does not have moderator privileges");
    }

    // Get the change request
    ProductChangeRequest changeRequest =
        changeRequestRepository
            .findById(requestId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Change request not found with id: " + requestId));

    // Validate it's still pending
    if (changeRequest.getStatus() != ProductChangeStatus.PENDING) {
      throw new IllegalStateException(
          "Change request has already been reviewed with status: " + changeRequest.getStatus());
    }

    // Update the change request status (do NOT apply changes to product)
    changeRequest.setStatus(ProductChangeStatus.REJECTED);
    changeRequest.setModerator(moderator);
    changeRequest.setModeratorComments(approvalDTO.getComments());
    changeRequest.setReviewedAt(LocalDateTime.now());

    ProductChangeRequest updated = changeRequestRepository.save(changeRequest);
    return mapper.toResponseDTO(updated);
  }
}
