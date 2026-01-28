package com.hasandag.ecommerce.repository.specification;

import com.hasandag.ecommerce.dto.OrderFilterDTO;
import com.hasandag.ecommerce.entity.Order;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecification {

  public static Specification<Order> buildSpecification(OrderFilterDTO filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (filter.getOrderNumber() != null && !filter.getOrderNumber().isBlank()) {
        predicates.add(
            cb.like(
                cb.lower(root.get("orderNumber")),
                "%" + filter.getOrderNumber().toLowerCase() + "%"));
      }

      if (filter.getStatus() != null) {
        predicates.add(cb.equal(root.get("status"), filter.getStatus()));
      }

      if (filter.getMinTotalAmount() != null) {
        predicates.add(
            cb.greaterThanOrEqualTo(root.get("totalAmount"), filter.getMinTotalAmount()));
      }

      if (filter.getMaxTotalAmount() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("totalAmount"), filter.getMaxTotalAmount()));
      }

      if (filter.getOrderDateFrom() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), filter.getOrderDateFrom()));
      }

      if (filter.getOrderDateTo() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), filter.getOrderDateTo()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
