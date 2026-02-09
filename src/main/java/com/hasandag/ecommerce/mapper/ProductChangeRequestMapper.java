package com.hasandag.ecommerce.mapper;

import com.hasandag.ecommerce.dto.ProductChangeRequestCreateDTO;
import com.hasandag.ecommerce.dto.ProductChangeRequestResponseDTO;
import com.hasandag.ecommerce.entity.ProductChangeRequest;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {ProductMapper.class})
public interface ProductChangeRequestMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "product", ignore = true)
  @Mapping(target = "proposedCategory", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "reviewedBy", ignore = true)
  @Mapping(target = "rejectionReason", ignore = true)
  @Mapping(target = "requestedAt", ignore = true)
  @Mapping(target = "reviewedAt", ignore = true)
  @Mapping(target = "proposedName", source = "name")
  @Mapping(target = "proposedDescription", source = "description")
  @Mapping(target = "proposedPrice", source = "price")
  @Mapping(target = "proposedStock", source = "stock")
  ProductChangeRequest toEntity(ProductChangeRequestCreateDTO dto);

  @Mapping(target = "productId", source = "product.id")
  @Mapping(target = "proposedCategoryId", source = "proposedCategory.id")
  @Mapping(target = "proposedCategoryName", source = "proposedCategory.name")
  @Mapping(target = "originalProduct", ignore = true)
  ProductChangeRequestResponseDTO toResponseDTO(ProductChangeRequest entity);
}
