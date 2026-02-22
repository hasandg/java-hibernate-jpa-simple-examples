package com.hasandag.ecommerce.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

  private static final int ORDER_NUMBER_LENGTH = 8;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String orderNumber;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  @Column(nullable = false)
  private LocalDateTime orderDate;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> orderItems = new ArrayList<>();

  @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private OrderDetail orderDetail;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  public static Order create(Customer customer) {
    Order order = new Order();
    order.orderNumber =
        "ORD-" + UUID.randomUUID().toString().substring(0, ORDER_NUMBER_LENGTH).toUpperCase();
    order.status = OrderStatus.PENDING;
    order.orderDate = LocalDateTime.now();
    order.totalAmount = BigDecimal.ZERO;
    order.customer = customer;
    return order;
  }

  public void addItem(OrderItem item) {
    item.setOrder(this);
    orderItems.add(item);
    recalculateTotalAmount();
  }

  public void attachDetail(OrderDetail detail) {
    detail.setOrder(this);
    this.orderDetail = detail;
  }

  public void ensureModifiable() {
    if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
      throw new IllegalStateException("Cannot update order with status: " + status);
    }
  }

  public void ensureDeletable() {
    if (status == OrderStatus.DELIVERED) {
      throw new IllegalStateException("Cannot delete delivered order with id: " + id);
    }
  }

  public void restoreStock() {
    for (OrderItem item : orderItems) {
      item.getProduct().restoreStock(item.getQuantity());
    }
  }

  private void recalculateTotalAmount() {
    this.totalAmount =
        orderItems.stream().map(OrderItem::calculateTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
