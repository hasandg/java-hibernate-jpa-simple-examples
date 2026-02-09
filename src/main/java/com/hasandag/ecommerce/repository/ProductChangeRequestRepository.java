package com.hasandag.ecommerce.repository;

import com.hasandag.ecommerce.entity.ChangeRequestStatus;
import com.hasandag.ecommerce.entity.ProductChangeRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductChangeRequestRepository extends JpaRepository<ProductChangeRequest, Long> {
  List<ProductChangeRequest> findByStatus(ChangeRequestStatus status);

  List<ProductChangeRequest> findByProductIdAndStatus(Long productId, ChangeRequestStatus status);
}
