package com.hasandag.ecommerce.mapper;

import com.hasandag.ecommerce.dto.OrderItemCreateDTO;
import com.hasandag.ecommerce.dto.OrderItemResponseDTO;
import com.hasandag.ecommerce.entity.OrderItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "unitPrice", ignore = true)
  @Mapping(target = "order", ignore = true)
  @Mapping(target = "product", ignore = true)
  OrderItem toEntity(OrderItemCreateDTO dto);

  @Mapping(target = "productId", source = "product.id")
  @Mapping(target = "productName", source = "product.name")
  OrderItemResponseDTO toResponseDTO(OrderItem orderItem);
}
