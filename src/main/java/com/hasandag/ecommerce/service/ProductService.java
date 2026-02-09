package com.hasandag.ecommerce.service;

import com.hasandag.ecommerce.dto.FilterDTO;
import com.hasandag.ecommerce.dto.PageResponseDTO;
import com.hasandag.ecommerce.dto.ProductCreateDTO;
import com.hasandag.ecommerce.dto.ProductDraftResponseDTO;
import com.hasandag.ecommerce.dto.ProductDraftSubmitDTO;
import com.hasandag.ecommerce.dto.ProductResponseDTO;
import com.hasandag.ecommerce.dto.ProductUpdateDTO;
import com.hasandag.ecommerce.entity.Category;
import com.hasandag.ecommerce.entity.Product;
import com.hasandag.ecommerce.entity.ProductDraft;
import com.hasandag.ecommerce.entity.ProductDraftStatus;
import com.hasandag.ecommerce.mapper.ProductMapper;
import com.hasandag.ecommerce.repository.CategoryRepository;
import com.hasandag.ecommerce.repository.ProductDraftRepository;
import com.hasandag.ecommerce.repository.ProductRepository;
import com.hasandag.ecommerce.repository.specification.GenericSpecificationBuilder;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Path;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductDraftRepository productDraftRepository;
  private final CategoryRepository categoryRepository;
  private final ProductMapper productMapper;

  @Transactional
  public ProductResponseDTO create(ProductCreateDTO createDTO) {
    Product product = productMapper.toEntity(createDTO);

    if (createDTO.getCategoryId() != null) {
      Category category =
          categoryRepository
              .findById(createDTO.getCategoryId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Category not found with id: " + createDTO.getCategoryId()));
      product.setCategory(category);
    }

    Product savedProduct = productRepository.save(product);
    return productMapper.toResponseDTO(savedProduct);
  }

  @Transactional(readOnly = true)
  public ProductResponseDTO findById(Long id) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    return productMapper.toResponseDTO(product);
  }

  @Transactional(readOnly = true)
  public List<ProductResponseDTO> findAll() {
    return productRepository.findAll().stream().map(productMapper::toResponseDTO).toList();
  }

  @Transactional(readOnly = true)
  public List<ProductResponseDTO> findByCategoryId(Long categoryId) {
    return productRepository.findByCategoryId(categoryId).stream()
        .map(productMapper::toResponseDTO)
        .toList();
  }

  @Transactional
  public ProductResponseDTO update(ProductUpdateDTO updateDTO) {
    if (updateDTO.getId() == null) {
      throw new IllegalArgumentException("Product ID is required for update");
    }

    Product product =
        productRepository
            .findById(updateDTO.getId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Product not found with id: " + updateDTO.getId()));

    productMapper.updateEntityFromDTO(updateDTO, product);

    if (updateDTO.getCategoryId() != null) {
      Category category =
          categoryRepository
              .findById(updateDTO.getCategoryId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Category not found with id: " + updateDTO.getCategoryId()));
      product.setCategory(category);
    } else {
      product.setCategory(null);
    }

    Product updatedProduct = productRepository.save(product);
    return productMapper.toResponseDTO(updatedProduct);
  }

  @Transactional
  public ProductDraftResponseDTO submitDraft(ProductDraftSubmitDTO submitDTO) {
    Product product =
        productRepository
            .findById(submitDTO.getId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Product not found with id: " + submitDTO.getId()));

    Category category = resolveCategory(submitDTO.getCategoryId());

    ProductDraft draft = new ProductDraft();
    draft.setProduct(product);
    draft.setName(submitDTO.getName());
    draft.setDescription(submitDTO.getDescription());
    draft.setPrice(submitDTO.getPrice());
    draft.setStock(submitDTO.getStock());
    draft.setCategory(category);
    draft.setStatus(ProductDraftStatus.PENDING);
    draft.setSubmittedBy(submitDTO.getSubmittedBy());
    draft.setSubmittedAt(LocalDateTime.now());

    ProductDraft savedDraft = productDraftRepository.save(draft);
    return toDraftResponseDTO(savedDraft);
  }

  @Transactional(readOnly = true)
  public List<ProductDraftResponseDTO> findPendingDrafts() {
    return productDraftRepository
        .findByStatusOrderBySubmittedAtAsc(ProductDraftStatus.PENDING)
        .stream()
        .map(this::toDraftResponseDTO)
        .toList();
  }

  @Transactional
  public ProductResponseDTO approveDraft(Long draftId, String moderatorName) {
    ProductDraft draft = getPendingDraftOrThrow(draftId);
    Product product = draft.getProduct();

    product.setName(draft.getName());
    product.setDescription(draft.getDescription());
    product.setPrice(draft.getPrice());
    product.setStock(draft.getStock());
    product.setCategory(draft.getCategory());
    Product updatedProduct = productRepository.save(product);

    draft.setStatus(ProductDraftStatus.APPROVED);
    draft.setReviewedBy(moderatorName);
    draft.setReviewedAt(LocalDateTime.now());
    draft.setRejectionReason(null);
    productDraftRepository.save(draft);

    return productMapper.toResponseDTO(updatedProduct);
  }

  @Transactional
  public ProductDraftResponseDTO rejectDraft(
      Long draftId, String moderatorName, String rejectionReason) {
    ProductDraft draft = getPendingDraftOrThrow(draftId);
    draft.setStatus(ProductDraftStatus.REJECTED);
    draft.setReviewedBy(moderatorName);
    draft.setReviewedAt(LocalDateTime.now());
    draft.setRejectionReason(rejectionReason);
    ProductDraft savedDraft = productDraftRepository.save(draft);
    return toDraftResponseDTO(savedDraft);
  }

  @Transactional(readOnly = true)
  public PageResponseDTO<ProductResponseDTO> filter(FilterDTO filterDTO) {
    if (filterDTO == null) {
      filterDTO = new FilterDTO();
    }
    final FilterDTO finalFilterDTO = filterDTO;

    GenericSpecificationBuilder.FieldMappingConfig<Product> config =
        new GenericSpecificationBuilder.FieldMappingConfig<Product>()
            .addMapping("name", "name")
            .addMapping(
                "minPrice",
                new GenericSpecificationBuilder.FieldMapping<Product>("price")
                    .withAdvancedPredicate(
                        (context) -> {
                          BigDecimal minPrice = parseBigDecimal(context.getValue());
                          return minPrice != null
                              ? context
                                  .getCriteriaBuilder()
                                  .greaterThanOrEqualTo(
                                      (Path<BigDecimal>) context.getFieldPath(), minPrice)
                              : null;
                        }))
            .addMapping(
                "maxPrice",
                new GenericSpecificationBuilder.FieldMapping<Product>("price")
                    .withAdvancedPredicate(
                        (context) -> {
                          BigDecimal maxPrice = parseBigDecimal(context.getValue());
                          return maxPrice != null
                              ? context
                                  .getCriteriaBuilder()
                                  .lessThanOrEqualTo(
                                      (Path<BigDecimal>) context.getFieldPath(), maxPrice)
                              : null;
                        }))
            .addMapping(
                "minStock",
                new GenericSpecificationBuilder.FieldMapping<Product>("stock")
                    .withAdvancedPredicate(
                        (context) -> {
                          Integer minStock = finalFilterDTO.getIntegerFilter("minStock");
                          return minStock != null
                              ? context
                                  .getCriteriaBuilder()
                                  .greaterThanOrEqualTo(
                                      (Path<Integer>) context.getFieldPath(), minStock)
                              : null;
                        }))
            .addMapping(
                "maxStock",
                new GenericSpecificationBuilder.FieldMapping<Product>("stock")
                    .withAdvancedPredicate(
                        (context) -> {
                          Integer maxStock = finalFilterDTO.getIntegerFilter("maxStock");
                          return maxStock != null
                              ? context
                                  .getCriteriaBuilder()
                                  .lessThanOrEqualTo(
                                      (Path<Integer>) context.getFieldPath(), maxStock)
                              : null;
                        }))
            .addMapping(
                "categoryId",
                new GenericSpecificationBuilder.FieldMapping<Product>("id")
                    .withJoin("category", jakarta.persistence.criteria.JoinType.INNER)
                    .withAdvancedPredicate(
                        (context) -> {
                          Long categoryId = finalFilterDTO.getLongFilter("categoryId");
                          return categoryId != null
                              ? context
                                  .getCriteriaBuilder()
                                  .equal(context.getRoot().get("category").get("id"), categoryId)
                              : null;
                        }));

    Specification<Product> spec = GenericSpecificationBuilder.buildSpecification(filterDTO, config);

    Pageable pageable =
        PageRequest.of(
            filterDTO.getPage() != null ? filterDTO.getPage() : 0,
            filterDTO.getSize() != null ? filterDTO.getSize() : 10);
    Page<Product> page = productRepository.findAll(spec, pageable);
    List<ProductResponseDTO> content =
        page.getContent().stream().map(productMapper::toResponseDTO).toList();
    return new PageResponseDTO<>(
        content,
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast());
  }

  private BigDecimal parseBigDecimal(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof BigDecimal) {
      return (BigDecimal) value;
    }
    if (value instanceof Number) {
      return BigDecimal.valueOf(((Number) value).doubleValue());
    }
    try {
      return new BigDecimal(value.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  @Transactional
  public void delete(List<Long> ids) {
    for (Long id : ids) {
      if (!productRepository.existsById(id)) {
        throw new EntityNotFoundException("Product not found with id: " + id);
      }
    }
    productRepository.deleteAllById(ids);
  }

  private ProductDraft getPendingDraftOrThrow(Long draftId) {
    ProductDraft draft =
        productDraftRepository
            .findById(draftId)
            .orElseThrow(
                () -> new EntityNotFoundException("Product draft not found with id: " + draftId));
    if (draft.getStatus() != ProductDraftStatus.PENDING) {
      throw new IllegalStateException("Only pending drafts can be reviewed");
    }
    return draft;
  }

  private Category resolveCategory(Long categoryId) {
    if (categoryId == null) {
      return null;
    }
    return categoryRepository
        .findById(categoryId)
        .orElseThrow(
            () -> new EntityNotFoundException("Category not found with id: " + categoryId));
  }

  private ProductDraftResponseDTO toDraftResponseDTO(ProductDraft draft) {
    ProductDraftResponseDTO response = new ProductDraftResponseDTO();
    response.setId(draft.getId());
    response.setProductId(draft.getProduct().getId());
    response.setName(draft.getName());
    response.setDescription(draft.getDescription());
    response.setPrice(draft.getPrice());
    response.setStock(draft.getStock());
    response.setCategoryId(draft.getCategory() != null ? draft.getCategory().getId() : null);
    response.setCategoryName(draft.getCategory() != null ? draft.getCategory().getName() : null);
    response.setStatus(draft.getStatus().name());
    response.setSubmittedBy(draft.getSubmittedBy());
    response.setSubmittedAt(draft.getSubmittedAt());
    response.setReviewedBy(draft.getReviewedBy());
    response.setReviewedAt(draft.getReviewedAt());
    response.setRejectionReason(draft.getRejectionReason());
    return response;
  }
}
