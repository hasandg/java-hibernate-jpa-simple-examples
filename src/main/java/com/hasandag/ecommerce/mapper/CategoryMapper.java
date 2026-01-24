package com.hasandag.ecommerce.mapper;

import com.hasandag.ecommerce.domain.Category;
import com.hasandag.ecommerce.dto.CategoryCreateDTO;
import com.hasandag.ecommerce.dto.CategoryResponseDTO;
import com.hasandag.ecommerce.dto.CategoryUpdateDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "products", ignore = true)
  Category toEntity(CategoryCreateDTO dto);

  @Mapping(
      target = "productCount",
      expression = "java(category.getProducts() != null ? category.getProducts().size() : 0)")
  CategoryResponseDTO toResponseDTO(Category category);

  @Mapping(target = "products", ignore = true)
  void updateEntityFromDTO(CategoryUpdateDTO dto, @MappingTarget Category category);
}
