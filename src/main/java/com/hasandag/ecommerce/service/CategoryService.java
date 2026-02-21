package com.hasandag.ecommerce.service;

import com.hasandag.ecommerce.dto.CategoryCreateDTO;
import com.hasandag.ecommerce.dto.CategoryResponseDTO;
import com.hasandag.ecommerce.dto.CategoryUpdateDTO;
import com.hasandag.ecommerce.dto.FilterDTO;
import com.hasandag.ecommerce.dto.PageResponseDTO;
import com.hasandag.ecommerce.entity.Category;
import com.hasandag.ecommerce.entity.Product;
import com.hasandag.ecommerce.mapper.CategoryMapper;
import com.hasandag.ecommerce.repository.CategoryRepository;
import com.hasandag.ecommerce.repository.specification.GenericSpecificationBuilder;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Subquery;
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
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

  @Transactional
  public CategoryResponseDTO create(CategoryCreateDTO createDTO) {
    if (categoryRepository.findByName(createDTO.getName()).isPresent()) {
      throw new IllegalArgumentException(
          "Category with name '" + createDTO.getName() + "' already exists");
    }

    Category category = categoryMapper.toEntity(createDTO);
    Category savedCategory = categoryRepository.save(category);
    return categoryMapper.toResponseDTO(savedCategory);
  }

  @Transactional(readOnly = true)
  public CategoryResponseDTO findById(Long id) {
    Category category =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    return categoryMapper.toResponseDTO(category);
  }

  @Transactional(readOnly = true)
  public List<CategoryResponseDTO> findAll() {
    return categoryRepository.findAll().stream().map(categoryMapper::toResponseDTO).toList();
  }

  @Transactional
  public CategoryResponseDTO update(CategoryUpdateDTO updateDTO) {
    if (updateDTO.getId() == null) {
      throw new IllegalArgumentException("Category ID is required for update");
    }

    Category category =
        categoryRepository
            .findById(updateDTO.getId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Category not found with id: " + updateDTO.getId()));

    if (!category.getName().equals(updateDTO.getName())) {
      if (categoryRepository.findByName(updateDTO.getName()).isPresent()) {
        throw new IllegalArgumentException(
            "Category with name '" + updateDTO.getName() + "' already exists");
      }
    }

    categoryMapper.updateEntityFromDTO(updateDTO, category);
    Category updatedCategory = categoryRepository.save(category);
    return categoryMapper.toResponseDTO(updatedCategory);
  }

  @Transactional(readOnly = true)
  public PageResponseDTO<CategoryResponseDTO> filter(FilterDTO filterDTO) {
    final FilterDTO finalFilterDTO = filterDTO;

    GenericSpecificationBuilder.FieldMappingConfig<Category> config =
        new GenericSpecificationBuilder.FieldMappingConfig<Category>()
            .addMapping("name", "name")
            .addMapping(
                "minProductCount",
                new GenericSpecificationBuilder.FieldMapping<Category>("id")
                    .withAdvancedPredicate(
                        (context) -> {
                          Integer minCount = finalFilterDTO.getIntegerFilter("minProductCount");
                          if (minCount == null) {
                            return null;
                          }
                          Subquery<Long> subquery = context.getQuery().subquery(Long.class);
                          Path<Product> productRoot = subquery.from(Product.class);
                          subquery.select(context.getCriteriaBuilder().count(productRoot));
                          subquery.where(
                              context
                                  .getCriteriaBuilder()
                                  .equal(
                                      productRoot.get("category").get("id"),
                                      context.getRoot().get("id")));
                          return context
                              .getCriteriaBuilder()
                              .greaterThanOrEqualTo(subquery, (long) minCount);
                        }))
            .addMapping(
                "maxProductCount",
                new GenericSpecificationBuilder.FieldMapping<Category>("id")
                    .withAdvancedPredicate(
                        (context) -> {
                          Integer maxCount = finalFilterDTO.getIntegerFilter("maxProductCount");
                          if (maxCount == null) {
                            return null;
                          }
                          Subquery<Long> subquery = context.getQuery().subquery(Long.class);
                          Path<Product> productRoot = subquery.from(Product.class);
                          subquery.select(context.getCriteriaBuilder().count(productRoot));
                          subquery.where(
                              context
                                  .getCriteriaBuilder()
                                  .equal(
                                      productRoot.get("category").get("id"),
                                      context.getRoot().get("id")));
                          return context
                              .getCriteriaBuilder()
                              .lessThanOrEqualTo(subquery, (long) maxCount);
                        }));

    Specification<Category> spec =
        GenericSpecificationBuilder.buildSpecification(filterDTO, config);

    Pageable pageable = PageRequest.of(filterDTO.getPage(), filterDTO.getSize());
    Page<Category> page = categoryRepository.findAll(spec, pageable);
    List<CategoryResponseDTO> content =
        page.getContent().stream().map(categoryMapper::toResponseDTO).toList();
    return new PageResponseDTO<>(
        content,
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast());
  }

  @Transactional
  public void delete(List<Long> ids) {
    for (Long id : ids) {
      Category category =
          categoryRepository
              .findById(id)
              .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));

      if (!category.getProducts().isEmpty()) {
        throw new IllegalStateException(
            "Cannot delete category with id " + id + " - it has associated products");
      }
    }
    categoryRepository.deleteAllById(ids);
  }
}
