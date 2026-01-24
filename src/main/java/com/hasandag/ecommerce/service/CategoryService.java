package com.hasandag.ecommerce.service;

import com.hasandag.ecommerce.domain.Category;
import com.hasandag.ecommerce.dto.CategoryCreateDTO;
import com.hasandag.ecommerce.dto.CategoryResponseDTO;
import com.hasandag.ecommerce.dto.CategoryUpdateDTO;
import com.hasandag.ecommerce.mapper.CategoryMapper;
import com.hasandag.ecommerce.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

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
