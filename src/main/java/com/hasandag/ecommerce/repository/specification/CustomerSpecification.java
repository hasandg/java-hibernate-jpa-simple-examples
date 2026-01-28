package com.hasandag.ecommerce.repository.specification;

import com.hasandag.ecommerce.dto.CustomerFilterDTO;
import com.hasandag.ecommerce.entity.Customer;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class CustomerSpecification {

  public static Specification<Customer> buildSpecification(CustomerFilterDTO filter) {
    if (filter == null) {
      return (root, query, cb) -> cb.conjunction();
    }

    return (root, query, cb) -> {
      System.out.println("DEBUG Specification: filter = " + filter);
      System.out.println("DEBUG Specification: filter.getFirstName() = " + filter.getFirstName());
      List<Predicate> predicates = new ArrayList<>();
      Join<Object, Object> addressJoin = null;

      String firstName = filter.getFirstName();
      System.out.println("DEBUG Specification: firstName = " + firstName);
      if (firstName != null && !firstName.trim().isEmpty()) {
        String pattern = "%" + firstName.trim().toLowerCase() + "%";
        System.out.println("DEBUG Specification: Adding predicate with pattern: " + pattern);
        predicates.add(cb.like(cb.lower(root.get("firstName")), pattern));
      } else {
        System.out.println("DEBUG Specification: firstName is null or empty, skipping predicate");
      }

      if (filter.getLastName() != null && !filter.getLastName().trim().isEmpty()) {
        predicates.add(
            cb.like(
                cb.lower(root.get("lastName")),
                "%" + filter.getLastName().trim().toLowerCase() + "%"));
      }

      if (filter.getEmail() != null && !filter.getEmail().trim().isEmpty()) {
        predicates.add(
            cb.like(
                cb.lower(root.get("email")), "%" + filter.getEmail().trim().toLowerCase() + "%"));
      }

      if (filter.getPhone() != null && !filter.getPhone().trim().isEmpty()) {
        predicates.add(cb.like(root.get("phone"), "%" + filter.getPhone().trim() + "%"));
      }

      boolean needsAddressJoin =
          (filter.getCity() != null && !filter.getCity().trim().isEmpty())
              || (filter.getCountry() != null && !filter.getCountry().trim().isEmpty());

      if (needsAddressJoin) {
        addressJoin = root.join("address", JoinType.LEFT);
      }

      if (filter.getCity() != null && !filter.getCity().trim().isEmpty() && addressJoin != null) {
        predicates.add(
            cb.like(
                cb.lower(addressJoin.get("city")),
                "%" + filter.getCity().trim().toLowerCase() + "%"));
      }

      if (filter.getCountry() != null
          && !filter.getCountry().trim().isEmpty()
          && addressJoin != null) {
        predicates.add(
            cb.like(
                cb.lower(addressJoin.get("country")),
                "%" + filter.getCountry().trim().toLowerCase() + "%"));
      }

      System.out.println("DEBUG Specification: predicates.size() = " + predicates.size());

      if (predicates.isEmpty()) {
        System.out.println("DEBUG Specification: No predicates, returning conjunction (1=1)");
        return cb.conjunction();
      }

      System.out.println(
          "DEBUG Specification: Returning AND predicate with " + predicates.size() + " predicates");
      query.distinct(true);
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
