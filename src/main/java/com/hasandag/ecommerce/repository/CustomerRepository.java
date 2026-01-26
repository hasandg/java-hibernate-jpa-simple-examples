package com.hasandag.ecommerce.repository;

import com.hasandag.ecommerce.entity.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
  Optional<Customer> findByEmail(String email);
}
