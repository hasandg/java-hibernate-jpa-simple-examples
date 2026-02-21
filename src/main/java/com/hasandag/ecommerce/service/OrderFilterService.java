package com.hasandag.ecommerce.service;

import com.hasandag.ecommerce.dto.FilterDTO;
import com.hasandag.ecommerce.dto.OrderResponseDTO;
import com.hasandag.ecommerce.dto.PageResponseDTO;
import com.hasandag.ecommerce.entity.Order;
import com.hasandag.ecommerce.entity.OrderStatus;
import com.hasandag.ecommerce.mapper.OrderMapper;
import com.hasandag.ecommerce.repository.OrderRepository;
import com.hasandag.ecommerce.repository.specification.GenericSpecificationBuilder;
import jakarta.persistence.criteria.JoinType;
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
public class OrderFilterService {

  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;

  @Transactional(readOnly = true)
  public PageResponseDTO<OrderResponseDTO> filter(FilterDTO filterDTO) {
    Specification<Order> spec =
        GenericSpecificationBuilder.buildSpecification(filterDTO, buildFilterConfig());

    Pageable pageable = PageRequest.of(filterDTO.getPage(), filterDTO.getSize());
    Page<Order> page = orderRepository.findAll(spec, pageable);
    List<OrderResponseDTO> content =
        page.getContent().stream().map(orderMapper::toResponseDTO).toList();
    return new PageResponseDTO<>(
        content,
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast());
  }

  private GenericSpecificationBuilder.FieldMappingConfig<Order> buildFilterConfig() {
    return new GenericSpecificationBuilder.FieldMappingConfig<Order>()
        .addMapping("orderNumber", "orderNumber")
        .addMapping(
            "status",
            new GenericSpecificationBuilder.FieldMapping<Order>("status")
                .caseSensitive()
                .withAdvancedPredicate(ctx -> ctx.enumEquals(OrderStatus.class)))
        .addMapping(
            "minTotalAmount",
            new GenericSpecificationBuilder.FieldMapping<Order>("totalAmount")
                .withAdvancedPredicate(
                    GenericSpecificationBuilder.PredicateContext::greaterThanOrEqualBigDecimal))
        .addMapping(
            "maxTotalAmount",
            new GenericSpecificationBuilder.FieldMapping<Order>("totalAmount")
                .withAdvancedPredicate(
                    GenericSpecificationBuilder.PredicateContext::lessThanOrEqualBigDecimal))
        .addMapping(
            "orderDateFrom",
            new GenericSpecificationBuilder.FieldMapping<Order>("orderDate")
                .withAdvancedPredicate(
                    GenericSpecificationBuilder.PredicateContext::greaterThanOrEqualDateTime))
        .addMapping(
            "orderDateTo",
            new GenericSpecificationBuilder.FieldMapping<Order>("orderDate")
                .withAdvancedPredicate(
                    GenericSpecificationBuilder.PredicateContext::lessThanOrEqualDateTime))
        .addMapping(
            "customerId",
            new GenericSpecificationBuilder.FieldMapping<Order>("id")
                .withJoin("customer", JoinType.INNER)
                .withAdvancedPredicate(GenericSpecificationBuilder.PredicateContext::equalLong))
        .addMapping(
            "notes",
            new GenericSpecificationBuilder.FieldMapping<Order>("notes")
                .withJoin("orderDetail", JoinType.LEFT))
        .addMapping(
            "shippingMethod",
            new GenericSpecificationBuilder.FieldMapping<Order>("shippingMethod")
                .withJoin("orderDetail", JoinType.LEFT));
  }
}
