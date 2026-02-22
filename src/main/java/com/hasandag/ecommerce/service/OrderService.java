package com.hasandag.ecommerce.service;

import com.hasandag.ecommerce.dto.OrderCreateDTO;
import com.hasandag.ecommerce.dto.OrderItemCreateDTO;
import com.hasandag.ecommerce.dto.OrderResponseDTO;
import com.hasandag.ecommerce.dto.OrderUpdateDTO;
import com.hasandag.ecommerce.entity.Customer;
import com.hasandag.ecommerce.entity.Order;
import com.hasandag.ecommerce.entity.OrderDetail;
import com.hasandag.ecommerce.entity.OrderItem;
import com.hasandag.ecommerce.entity.Product;
import com.hasandag.ecommerce.mapper.OrderItemMapper;
import com.hasandag.ecommerce.mapper.OrderMapper;
import com.hasandag.ecommerce.repository.CustomerRepository;
import com.hasandag.ecommerce.repository.OrderRepository;
import com.hasandag.ecommerce.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final CustomerRepository customerRepository;
  private final OrderMapper orderMapper;
  private final OrderItemMapper orderItemMapper;

  @Transactional
  public OrderResponseDTO create(OrderCreateDTO createDTO) {
    Customer customer = findCustomer(createDTO.getCustomerId());
    Order order = Order.create(customer);

    for (OrderItemCreateDTO itemDTO : createDTO.getOrderItems()) {
      Product product = findProduct(itemDTO.getProductId());
      OrderItem orderItem = orderItemMapper.toEntity(itemDTO);
      orderItem.fillFromProduct(product);
      order.addItem(orderItem);
    }

    if (createDTO.getNotes() != null || createDTO.getShippingMethod() != null) {
      order.attachDetail(orderMapper.toOrderDetail(createDTO));
    }

    return orderMapper.toResponseDTO(orderRepository.save(order));
  }

  @Transactional(readOnly = true)
  public OrderResponseDTO findById(Long id) {
    return orderMapper.toResponseDTO(findOrder(id));
  }

  @Transactional(readOnly = true)
  public List<OrderResponseDTO> findAll() {
    return orderRepository.findAll().stream().map(orderMapper::toResponseDTO).toList();
  }

  @Transactional
  public OrderResponseDTO update(OrderUpdateDTO updateDTO) {
    Order order = findOrder(updateDTO.getId());
    order.ensureModifiable();

    orderMapper.updateEntityFromDTO(updateDTO, order);

    if (updateDTO.getNotes() != null || updateDTO.getShippingMethod() != null) {
      OrderDetail orderDetail = order.getOrderDetail();
      if (orderDetail == null) {
        order.attachDetail(orderMapper.toOrderDetail(updateDTO));
      } else {
        orderMapper.updateOrderDetail(updateDTO, orderDetail);
      }
    }

    return orderMapper.toResponseDTO(orderRepository.save(order));
  }

  @Transactional
  public void delete(List<Long> ids) {
    for (Long id : ids) {
      Order order = findOrder(id);
      order.ensureDeletable();
      order.restoreStock();
    }
    orderRepository.deleteAllById(ids);
  }

  private Order findOrder(Long id) {
    return orderRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
  }

  private Customer findCustomer(Long id) {
    return customerRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));
  }

  private Product findProduct(Long id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
  }
}
