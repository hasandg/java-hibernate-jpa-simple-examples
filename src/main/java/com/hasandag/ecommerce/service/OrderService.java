package com.hasandag.ecommerce.service;

import com.hasandag.ecommerce.dto.OrderCreateDTO;
import com.hasandag.ecommerce.dto.OrderItemCreateDTO;
import com.hasandag.ecommerce.dto.OrderResponseDTO;
import com.hasandag.ecommerce.dto.OrderUpdateDTO;
import com.hasandag.ecommerce.entity.Order;
import com.hasandag.ecommerce.entity.OrderDetail;
import com.hasandag.ecommerce.entity.OrderItem;
import com.hasandag.ecommerce.entity.OrderStatus;
import com.hasandag.ecommerce.entity.Product;
import com.hasandag.ecommerce.mapper.OrderItemMapper;
import com.hasandag.ecommerce.mapper.OrderMapper;
import com.hasandag.ecommerce.repository.OrderRepository;
import com.hasandag.ecommerce.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final OrderMapper orderMapper;
  private final OrderItemMapper orderItemMapper;

  public OrderResponseDTO create(OrderCreateDTO createDTO) {
    Order order = new Order();
    order.setOrderNumber(generateOrderNumber());
    order.setStatus(OrderStatus.PENDING);
    order.setOrderDate(LocalDateTime.now());

    BigDecimal totalAmount = BigDecimal.ZERO;

    for (OrderItemCreateDTO itemDTO : createDTO.getOrderItems()) {
      Product product =
          productRepository
              .findById(itemDTO.getProductId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Product not found with id: " + itemDTO.getProductId()));

      if (product.getStock() < itemDTO.getQuantity()) {
        throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
      }

      OrderItem orderItem = orderItemMapper.toEntity(itemDTO);
      orderItem.setProduct(product);
      orderItem.setUnitPrice(product.getPrice());
      orderItem.setOrder(order);

      BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
      totalAmount = totalAmount.add(itemTotal);

      order.getOrderItems().add(orderItem);

      product.setStock(product.getStock() - itemDTO.getQuantity());
    }

    order.setTotalAmount(totalAmount);

    if (createDTO.getNotes() != null || createDTO.getShippingMethod() != null) {
      OrderDetail orderDetail = new OrderDetail();
      orderDetail.setNotes(createDTO.getNotes());
      orderDetail.setShippingMethod(createDTO.getShippingMethod());
      orderDetail.setOrder(order);
      order.setOrderDetail(orderDetail);
    }

    Order savedOrder = orderRepository.save(order);
    return orderMapper.toResponseDTO(savedOrder);
  }

  @Transactional(readOnly = true)
  public OrderResponseDTO findById(Long id) {
    Order order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
    return orderMapper.toResponseDTO(order);
  }

  @Transactional(readOnly = true)
  public List<OrderResponseDTO> findAll() {
    return orderRepository.findAll().stream().map(orderMapper::toResponseDTO).toList();
  }

  public OrderResponseDTO update(OrderUpdateDTO updateDTO) {
    if (updateDTO.getId() == null) {
      throw new IllegalArgumentException("Order ID is required for update");
    }

    Order order =
        orderRepository
            .findById(updateDTO.getId())
            .orElseThrow(
                () -> new EntityNotFoundException("Order not found with id: " + updateDTO.getId()));

    if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
      throw new IllegalStateException("Cannot update order with status: " + order.getStatus());
    }

    orderMapper.updateEntityFromDTO(updateDTO, order);

    if (updateDTO.getNotes() != null || updateDTO.getShippingMethod() != null) {
      OrderDetail orderDetail = order.getOrderDetail();
      if (orderDetail == null) {
        orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        order.setOrderDetail(orderDetail);
      }
      orderDetail.setNotes(updateDTO.getNotes());
      orderDetail.setShippingMethod(updateDTO.getShippingMethod());
    }

    Order updatedOrder = orderRepository.save(order);
    return orderMapper.toResponseDTO(updatedOrder);
  }

  public void delete(List<Long> ids) {
    for (Long id : ids) {
      Order order =
          orderRepository
              .findById(id)
              .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));

      if (order.getStatus() == OrderStatus.DELIVERED) {
        throw new IllegalStateException("Cannot delete delivered order with id: " + id);
      }

      for (OrderItem item : order.getOrderItems()) {
        Product product = item.getProduct();
        product.setStock(product.getStock() + item.getQuantity());
      }
    }
    orderRepository.deleteAllById(ids);
  }

  private String generateOrderNumber() {
    return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }
}
