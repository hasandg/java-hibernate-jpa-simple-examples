package com.hasandag.ecommerce.service;

import com.hasandag.ecommerce.dto.ChangeRequestReviewDTO;
import com.hasandag.ecommerce.dto.ProductChangeRequestCreateDTO;
import com.hasandag.ecommerce.dto.ProductChangeRequestResponseDTO;
import com.hasandag.ecommerce.dto.ProductResponseDTO;
import com.hasandag.ecommerce.entity.*;
import com.hasandag.ecommerce.mapper.ProductChangeRequestMapper;
import com.hasandag.ecommerce.mapper.ProductMapper;
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
public class ProductChangeRequestService {

  private final ProductChangeRequestRepository changeRequestRepository;
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductChangeRequestMapper changeRequestMapper;
  private final ProductMapper productMapper;

  @Transactional
  public ProductChangeRequestResponseDTO submitChangeRequest(
      ProductChangeRequestCreateDTO createDTO) {
    ProductChangeRequest changeRequest = changeRequestMapper.toEntity(createDTO);
    changeRequest.setStatus(ChangeRequestStatus.PENDING);
    changeRequest.setRequestedAt(LocalDateTime.now());

    if (createDTO.getType() == ChangeRequestType.UPDATE) {
      if (createDTO.getProductId() == null) {
        throw new IllegalArgumentException("Product ID is required for update requests");
      }
      Product product =
          productRepository
              .findById(createDTO.getProductId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Product not found with id: " + createDTO.getProductId()));
      changeRequest.setProduct(product);
    }

    if (createDTO.getCategoryId() != null) {
      Category category =
          categoryRepository
              .findById(createDTO.getCategoryId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Category not found with id: " + createDTO.getCategoryId()));
      changeRequest.setProposedCategory(category);
    }

    ProductChangeRequest saved = changeRequestRepository.save(changeRequest);
    return buildResponseDTO(saved);
  }

  @Transactional(readOnly = true)
  public ProductChangeRequestResponseDTO findById(Long id) {
    ProductChangeRequest changeRequest =
        changeRequestRepository
            .findById(id)
            .orElseThrow(
                () -> new EntityNotFoundException("Change request not found with id: " + id));
    return buildResponseDTO(changeRequest);
  }

  @Transactional(readOnly = true)
  public List<ProductChangeRequestResponseDTO> findPending() {
    return changeRequestRepository.findByStatus(ChangeRequestStatus.PENDING).stream()
        .map(this::buildResponseDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ProductChangeRequestResponseDTO> findByStatus(ChangeRequestStatus status) {
    return changeRequestRepository.findByStatus(status).stream()
        .map(this::buildResponseDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ProductChangeRequestResponseDTO> findByProductId(Long productId) {
    return changeRequestRepository.findByProductId(productId).stream()
        .map(this::buildResponseDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ProductChangeRequestResponseDTO> findByRequestedBy(String requestedBy) {
    return changeRequestRepository.findByRequestedBy(requestedBy).stream()
        .map(this::buildResponseDTO)
        .toList();
  }

  @Transactional
  public ProductChangeRequestResponseDTO review(Long id, ChangeRequestReviewDTO reviewDTO) {
    ProductChangeRequest changeRequest =
        changeRequestRepository
            .findById(id)
            .orElseThrow(
                () -> new EntityNotFoundException("Change request not found with id: " + id));

    if (changeRequest.getStatus() != ChangeRequestStatus.PENDING) {
      throw new IllegalStateException(
          "Change request has already been " + changeRequest.getStatus().name().toLowerCase());
    }

    changeRequest.setReviewedBy(reviewDTO.getReviewedBy());
    changeRequest.setReviewedAt(LocalDateTime.now());

    if (Boolean.TRUE.equals(reviewDTO.getApproved())) {
      changeRequest.setStatus(ChangeRequestStatus.APPROVED);
      applyChanges(changeRequest);
    } else {
      if (reviewDTO.getRejectionReason() == null || reviewDTO.getRejectionReason().isBlank()) {
        throw new IllegalArgumentException("Rejection reason is required when rejecting a request");
      }
      changeRequest.setStatus(ChangeRequestStatus.REJECTED);
      changeRequest.setRejectionReason(reviewDTO.getRejectionReason());
    }

    ProductChangeRequest saved = changeRequestRepository.save(changeRequest);
    return buildResponseDTO(saved);
  }

  private void applyChanges(ProductChangeRequest changeRequest) {
    if (changeRequest.getType() == ChangeRequestType.CREATE) {
      Product product = new Product();
      product.setName(changeRequest.getProposedName());
      product.setDescription(changeRequest.getProposedDescription());
      product.setPrice(changeRequest.getProposedPrice());
      product.setStock(changeRequest.getProposedStock());
      product.setCategory(changeRequest.getProposedCategory());
      Product saved = productRepository.save(product);
      changeRequest.setProduct(saved);
    } else {
      Product product = changeRequest.getProduct();
      product.setName(changeRequest.getProposedName());
      product.setDescription(changeRequest.getProposedDescription());
      product.setPrice(changeRequest.getProposedPrice());
      product.setStock(changeRequest.getProposedStock());
      product.setCategory(changeRequest.getProposedCategory());
      productRepository.save(product);
    }
  }

  private ProductChangeRequestResponseDTO buildResponseDTO(ProductChangeRequest changeRequest) {
    ProductChangeRequestResponseDTO dto = changeRequestMapper.toResponseDTO(changeRequest);

    if (changeRequest.getType() == ChangeRequestType.UPDATE && changeRequest.getProduct() != null) {
      ProductResponseDTO originalProduct = productMapper.toResponseDTO(changeRequest.getProduct());
      dto.setOriginalProduct(originalProduct);
    }

    return dto;
  }
}
