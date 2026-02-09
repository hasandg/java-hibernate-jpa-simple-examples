package com.hasandag.ecommerce.mapper;

import com.hasandag.ecommerce.dto.ProductChangeRequestCreateDTO;
import com.hasandag.ecommerce.dto.ProductChangeRequestResponseDTO;
import com.hasandag.ecommerce.entity.ProductChangeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductChangeRequestMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "product", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "moderator", ignore = true)
  @Mapping(target = "moderatorComments", ignore = true)
  @Mapping(target = "reviewedAt", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  ProductChangeRequest toEntity(ProductChangeRequestCreateDTO dto);

  @Mapping(source = "product.id", target = "productId")
  @Mapping(source = "product.name", target = "productName")
  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "user.username", target = "username")
  @Mapping(source = "product.name", target = "currentName")
  @Mapping(source = "product.description", target = "currentDescription")
  @Mapping(source = "product.price", target = "currentPrice")
  @Mapping(source = "product.stock", target = "currentStock")
  @Mapping(source = "moderator.id", target = "moderatorId")
  @Mapping(source = "moderator.username", target = "moderatorUsername")
  ProductChangeRequestResponseDTO toResponseDTO(ProductChangeRequest entity);
}
