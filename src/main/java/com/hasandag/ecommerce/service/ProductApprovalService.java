package com.hasandag.ecommerce.service;

import com.hasandag.ecommerce.dto.ProductChangeRequestResponseDTO;
import com.hasandag.ecommerce.dto.ProductUpdateDTO;
import com.hasandag.ecommerce.entity.Category;
import com.hasandag.ecommerce.entity.ChangeRequestStatus;
import com.hasandag.ecommerce.entity.Product;
import com.hasandag.ecommerce.entity.ProductChangeRequest;
import com.hasandag.ecommerce.repository.CategoryRepository;
import com.hasandag.ecommerce.repository.ProductChangeRequestRepository;
import com.hasandag.ecommerce.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductApprovalService {

  private final ProductChangeRequestRepository changeRequestRepository;
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;

  @Transactional
  public Long createChangeRequest(ProductUpdateDTO updateDTO, String requestedBy) {
    Product product =
        productRepository
            .findById(updateDTO.getId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Product not found with id: " + updateDTO.getId()));

    ProductChangeRequest request = new ProductChangeRequest();
    request.setProduct(product);
    request.setName(updateDTO.getName());
    request.setDescription(updateDTO.getDescription());
    request.setPrice(updateDTO.getPrice());
    request.setStock(updateDTO.getStock());
    request.setCategoryId(updateDTO.getCategoryId());
    request.setStatus(ChangeRequestStatus.PENDING);
    request.setRequestDate(LocalDateTime.now());
    request.setRequestedBy(requestedBy);

    ProductChangeRequest savedRequest = changeRequestRepository.save(request);
    return savedRequest.getId();
  }

  @Transactional(readOnly = true)
  public List<ProductChangeRequestResponseDTO> getPendingRequests() {
    return changeRequestRepository.findByStatus(ChangeRequestStatus.PENDING).stream()
        .map(this::toResponseDTO)
        .toList();
  }

  @Transactional
  public void approveRequest(Long requestId) {
    ProductChangeRequest request =
        changeRequestRepository
            .findById(requestId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Change request not found with id: " + requestId));

    if (request.getStatus() != ChangeRequestStatus.PENDING) {
      throw new IllegalStateException("Request is not in PENDING status");
    }

    Product product = request.getProduct();
    product.setName(request.getName());
    product.setDescription(request.getDescription());
    product.setPrice(request.getPrice());
    product.setStock(request.getStock());

    if (request.getCategoryId() != null) {
      Category category =
          categoryRepository
              .findById(request.getCategoryId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Category not found with id: " + request.getCategoryId()));
      product.setCategory(category);
    } else {
      product.setCategory(null);
    }

    productRepository.save(product);

    request.setStatus(ChangeRequestStatus.APPROVED);
    changeRequestRepository.save(request);
  }

  @Transactional
  public void rejectRequest(Long requestId) {
    ProductChangeRequest request =
        changeRequestRepository
            .findById(requestId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Change request not found with id: " + requestId));

    if (request.getStatus() != ChangeRequestStatus.PENDING) {
      throw new IllegalStateException("Request is not in PENDING status");
    }

    request.setStatus(ChangeRequestStatus.REJECTED);
    changeRequestRepository.save(request);
  }

  private ProductChangeRequestResponseDTO toResponseDTO(ProductChangeRequest request) {
    ProductChangeRequestResponseDTO dto = new ProductChangeRequestResponseDTO();
    dto.setId(request.getId());
    dto.setProductId(request.getProduct().getId());
    dto.setProductName(request.getProduct().getName());
    dto.setProposedName(request.getName());
    dto.setProposedDescription(request.getDescription());
    dto.setProposedPrice(request.getPrice());
    dto.setProposedStock(request.getStock());
    dto.setProposedCategoryId(request.getCategoryId());
    dto.setStatus(request.getStatus());
    dto.setRequestDate(request.getRequestDate());
    dto.setRequestedBy(request.getRequestedBy());
    return dto;
  }
}
