package com.hasandag.ecommerce.mapper;

import com.hasandag.ecommerce.domain.Product;
import com.hasandag.ecommerce.dto.ProductCreateDTO;
import com.hasandag.ecommerce.dto.ProductResponseDTO;
import com.hasandag.ecommerce.dto.ProductUpdateDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "category", ignore = true)
  @Mapping(target = "tags", ignore = true)
  Product toEntity(ProductCreateDTO dto);

  @Mapping(target = "categoryId", source = "category.id")
  @Mapping(target = "categoryName", source = "category.name")
  ProductResponseDTO toResponseDTO(Product product);

  @Mapping(target = "category", ignore = true)
  @Mapping(target = "tags", ignore = true)
  void updateEntityFromDTO(ProductUpdateDTO dto, @MappingTarget Product product);
}
