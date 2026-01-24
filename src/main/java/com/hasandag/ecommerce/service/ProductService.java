package com.hasandag.ecommerce.service;

import com.hasandag.ecommerce.domain.Category;
import com.hasandag.ecommerce.domain.Product;
import com.hasandag.ecommerce.dto.ProductCreateDTO;
import com.hasandag.ecommerce.dto.ProductResponseDTO;
import com.hasandag.ecommerce.dto.ProductUpdateDTO;
import com.hasandag.ecommerce.mapper.ProductMapper;
import com.hasandag.ecommerce.repository.CategoryRepository;
import com.hasandag.ecommerce.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductMapper productMapper;

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

  public void delete(List<Long> ids) {
    for (Long id : ids) {
      if (!productRepository.existsById(id)) {
        throw new EntityNotFoundException("Product not found with id: " + id);
      }
    }
    productRepository.deleteAllById(ids);
  }
}
