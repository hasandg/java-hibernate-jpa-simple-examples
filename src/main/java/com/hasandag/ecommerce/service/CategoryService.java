package com.hasandag.ecommerce.service;

import com.hasandag.ecommerce.dto.CategoryCreateDTO;
import com.hasandag.ecommerce.dto.CategoryResponseDTO;
import com.hasandag.ecommerce.dto.CategoryUpdateDTO;
import com.hasandag.ecommerce.dto.FilterDTO;
import com.hasandag.ecommerce.dto.PageResponseDTO;
import com.hasandag.ecommerce.entity.Category;
import com.hasandag.ecommerce.entity.Product;
import com.hasandag.ecommerce.mapper.CategoryMapper;
import com.hasandag.ecommerce.mapper.PageMapper;
import com.hasandag.ecommerce.repository.CategoryRepository;
import com.hasandag.ecommerce.repository.specification.FieldMapping;
import com.hasandag.ecommerce.repository.specification.FieldMappingConfig;
import com.hasandag.ecommerce.repository.specification.GenericSpecificationBuilder;
import com.hasandag.ecommerce.repository.specification.PredicateContext;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

  private static Predicate productCountPredicate(PredicateContext context, boolean isMin) {
    Integer count = PredicateContext.parseInteger(context.getValue());
    if (count == null) {
      return null;
    }
    Subquery<Long> subquery = context.getQuery().subquery(Long.class);
    Path<Product> productRoot = subquery.from(Product.class);
    subquery.select(context.getCriteriaBuilder().count(productRoot));
    subquery.where(
        context
            .getCriteriaBuilder()
            .equal(productRoot.get("category").get("id"), context.getRoot().get("id")));
    return isMin
        ? context.getCriteriaBuilder().greaterThanOrEqualTo(subquery, (long) count)
        : context.getCriteriaBuilder().lessThanOrEqualTo(subquery, (long) count);
  }

  @Transactional(readOnly = true)
  public PageResponseDTO<CategoryResponseDTO> filter(FilterDTO filterDTO) {
    FieldMappingConfig<Category> config =
        new FieldMappingConfig<>(Category.class)
            .addMapping(
                "minProductCount",
                new FieldMapping<Category>("id")
                    .withAdvancedPredicate(ctx -> productCountPredicate(ctx, true)))
            .addMapping(
                "maxProductCount",
                new FieldMapping<Category>("id")
                    .withAdvancedPredicate(ctx -> productCountPredicate(ctx, false)));

    Specification<Category> spec =
        GenericSpecificationBuilder.buildSpecification(filterDTO, config);
    return PageMapper.toPageResponseDTO(
        categoryRepository.findAll(spec, PageRequest.of(filterDTO.getPage(), filterDTO.getSize())),
        categoryMapper::toResponseDTO);
  }

  @Transactional
  public void delete(List<Long> ids) {
    for (Long id : ids) {
      Category category =
          categoryRepository
              .findById(id)
              .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
      category.ensureDeletable();
    }
    categoryRepository.deleteAllById(ids);
  }
}
