package com.hasandag.ecommerce.repository.specification;

import com.hasandag.ecommerce.dto.ProductFilterDTO;
import com.hasandag.ecommerce.entity.Product;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

  public static Specification<Product> buildSpecification(ProductFilterDTO filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (filter.getName() != null && !filter.getName().isBlank()) {
        predicates.add(
            cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
      }

      if (filter.getMinPrice() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
      }

      if (filter.getMaxPrice() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
      }

      if (filter.getMinStock() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("stock"), filter.getMinStock()));
      }

      if (filter.getMaxStock() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("stock"), filter.getMaxStock()));
      }

      if (filter.getCategoryId() != null) {
        predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
