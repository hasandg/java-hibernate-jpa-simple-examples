package com.hasandag.ecommerce.service;

import com.hasandag.ecommerce.dto.FilterDTO;
import com.hasandag.ecommerce.dto.PageResponseDTO;
import com.hasandag.ecommerce.dto.ProductCreateDTO;
import com.hasandag.ecommerce.dto.ProductResponseDTO;
import com.hasandag.ecommerce.dto.ProductUpdateDTO;
import com.hasandag.ecommerce.entity.Category;
import com.hasandag.ecommerce.entity.Product;
import com.hasandag.ecommerce.mapper.PageMapper;
import com.hasandag.ecommerce.mapper.ProductMapper;
import com.hasandag.ecommerce.repository.CategoryRepository;
import com.hasandag.ecommerce.repository.ProductRepository;
import com.hasandag.ecommerce.repository.specification.GenericSpecificationBuilder;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    Specification<Product> spec =
        GenericSpecificationBuilder.buildSpecification(filterDTO, Product.class);
    return PageMapper.toPageResponseDTO(
        productRepository.findAll(spec, PageRequest.of(filterDTO.getPage(), filterDTO.getSize())),
        productMapper::toResponseDTO);
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
