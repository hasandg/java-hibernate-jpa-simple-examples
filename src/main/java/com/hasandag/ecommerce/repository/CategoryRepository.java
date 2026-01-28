package com.hasandag.ecommerce.repository;

import com.hasandag.ecommerce.entity.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository
    extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
  Optional<Category> findByName(String name);
}
