package com.hasandag.ecommerce.repository.specification;

import com.hasandag.ecommerce.dto.CategoryFilterDTO;
import com.hasandag.ecommerce.entity.Category;
import com.hasandag.ecommerce.entity.Product;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class CategorySpecification {

  public static Specification<Category> buildSpecification(CategoryFilterDTO filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (filter.getName() != null && !filter.getName().isBlank()) {
        predicates.add(
            cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
      }

      if (filter.getMinProductCount() != null || filter.getMaxProductCount() != null) {
        Subquery<Long> productCountSubquery = query.subquery(Long.class);
        var productRoot = productCountSubquery.from(Product.class);
        productCountSubquery.select(cb.count(productRoot));
        productCountSubquery.where(cb.equal(productRoot.get("category").get("id"), root.get("id")));

        if (filter.getMinProductCount() != null) {
          predicates.add(
              cb.greaterThanOrEqualTo(productCountSubquery, (long) filter.getMinProductCount()));
        }
        if (filter.getMaxProductCount() != null) {
          predicates.add(
              cb.lessThanOrEqualTo(productCountSubquery, (long) filter.getMaxProductCount()));
        }
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
