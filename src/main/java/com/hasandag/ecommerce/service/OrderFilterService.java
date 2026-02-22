package com.hasandag.ecommerce.service;

import com.hasandag.ecommerce.dto.FilterDTO;
import com.hasandag.ecommerce.dto.OrderResponseDTO;
import com.hasandag.ecommerce.dto.PageResponseDTO;
import com.hasandag.ecommerce.entity.Order;
import com.hasandag.ecommerce.mapper.OrderMapper;
import com.hasandag.ecommerce.mapper.PageMapper;
import com.hasandag.ecommerce.repository.OrderRepository;
import com.hasandag.ecommerce.repository.specification.GenericSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
        GenericSpecificationBuilder.buildSpecification(filterDTO, Order.class);
    return PageMapper.toPageResponseDTO(
        orderRepository.findAll(spec, PageRequest.of(filterDTO.getPage(), filterDTO.getSize())),
        orderMapper::toResponseDTO);
  }
}
