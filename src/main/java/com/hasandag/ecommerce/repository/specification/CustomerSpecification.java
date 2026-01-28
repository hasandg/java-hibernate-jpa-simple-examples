package com.hasandag.ecommerce.repository.specification;

import com.hasandag.ecommerce.dto.CustomerFilterDTO;
import com.hasandag.ecommerce.entity.Customer;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class CustomerSpecification {

  public static Specification<Customer> buildSpecification(CustomerFilterDTO filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (filter.getFirstName() != null && !filter.getFirstName().isBlank()) {
        predicates.add(
            cb.like(
                cb.lower(root.get("firstName")), "%" + filter.getFirstName().toLowerCase() + "%"));
      }

      if (filter.getLastName() != null && !filter.getLastName().isBlank()) {
        predicates.add(
            cb.like(
                cb.lower(root.get("lastName")), "%" + filter.getLastName().toLowerCase() + "%"));
      }

      if (filter.getEmail() != null && !filter.getEmail().isBlank()) {
        predicates.add(
            cb.like(cb.lower(root.get("email")), "%" + filter.getEmail().toLowerCase() + "%"));
      }

      if (filter.getPhone() != null && !filter.getPhone().isBlank()) {
        predicates.add(cb.like(root.get("phone"), "%" + filter.getPhone() + "%"));
      }

      if (filter.getCity() != null && !filter.getCity().isBlank()) {
        predicates.add(
            cb.like(
                cb.lower(root.get("address").get("city")),
                "%" + filter.getCity().toLowerCase() + "%"));
      }

      if (filter.getCountry() != null && !filter.getCountry().isBlank()) {
        predicates.add(
            cb.like(
                cb.lower(root.get("address").get("country")),
                "%" + filter.getCountry().toLowerCase() + "%"));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
