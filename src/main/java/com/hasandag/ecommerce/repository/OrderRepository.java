package com.hasandag.ecommerce.repository;

import com.hasandag.ecommerce.entity.Order;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository
    extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
  Optional<Order> findByOrderNumber(String orderNumber);
}
