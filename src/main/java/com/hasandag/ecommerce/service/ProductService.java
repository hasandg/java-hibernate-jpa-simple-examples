package com.hasandag.ecommerce.service;

import com.hasandag.ecommerce.dto.FilterDTO;
import com.hasandag.ecommerce.dto.PageResponseDTO;
import com.hasandag.ecommerce.dto.ProductCreateDTO;
import com.hasandag.ecommerce.dto.ProductResponseDTO;
import com.hasandag.ecommerce.dto.ProductUpdateDTO;
import com.hasandag.ecommerce.entity.Category;
import com.hasandag.ecommerce.entity.Product;
import com.hasandag.ecommerce.mapper.ProductMapper;
import com.hasandag.ecommerce.repository.CategoryRepository;
import com.hasandag.ecommerce.repository.ProductRepository;
import com.hasandag.ecommerce.repository.specification.GenericSpecificationBuilder;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Path;
import java.math.BigDecimal;
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

  @Transactional(readOnly = true)
  public PageResponseDTO<ProductResponseDTO> filter(FilterDTO filterDTO) {
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

    Pageable pageable = PageRequest.of(filterDTO.getPage(), filterDTO.getSize());
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
}
