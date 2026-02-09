package com.hasandag.ecommerce.repository;

import com.hasandag.ecommerce.entity.ProductDraft;
import com.hasandag.ecommerce.entity.ProductDraftStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDraftRepository extends JpaRepository<ProductDraft, Long> {
  List<ProductDraft> findByStatusOrderBySubmittedAtAsc(ProductDraftStatus status);
}
