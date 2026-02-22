package com.hasandag.ecommerce.mapper;

import com.hasandag.ecommerce.dto.OrderCreateDTO;
import com.hasandag.ecommerce.dto.OrderResponseDTO;
import com.hasandag.ecommerce.dto.OrderUpdateDTO;
import com.hasandag.ecommerce.entity.Order;
import com.hasandag.ecommerce.entity.OrderDetail;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = OrderItemMapper.class)
public interface OrderMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "orderNumber", ignore = true)
  @Mapping(target = "totalAmount", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "orderDate", ignore = true)
  @Mapping(target = "orderItems", ignore = true)
  @Mapping(target = "orderDetail", ignore = true)
  Order toEntity(OrderCreateDTO dto);

  @Mapping(
      target = "notes",
      source = "orderDetail.notes",
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(
      target = "shippingMethod",
      source = "orderDetail.shippingMethod",
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "customerId", source = "customer.id")
  OrderResponseDTO toResponseDTO(Order order);

  @Mapping(target = "orderNumber", ignore = true)
  @Mapping(target = "totalAmount", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "orderDate", ignore = true)
  @Mapping(target = "orderItems", ignore = true)
  @Mapping(target = "orderDetail", ignore = true)
  void updateEntityFromDTO(OrderUpdateDTO dto, @MappingTarget Order order);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "order", ignore = true)
  OrderDetail toOrderDetail(OrderCreateDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "order", ignore = true)
  void updateOrderDetail(OrderUpdateDTO dto, @MappingTarget OrderDetail orderDetail);
}
