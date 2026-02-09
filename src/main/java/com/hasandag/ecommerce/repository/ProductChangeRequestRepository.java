package com.hasandag.ecommerce.repository;

import com.hasandag.ecommerce.entity.ProductChangeRequest;
import com.hasandag.ecommerce.entity.ProductChangeStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductChangeRequestRepository extends JpaRepository<ProductChangeRequest, Long> {

  List<ProductChangeRequest> findByStatus(ProductChangeStatus status);

  List<ProductChangeRequest> findByProductId(Long productId);

  List<ProductChangeRequest> findByUserId(Long userId);

  List<ProductChangeRequest> findByProductIdAndStatus(Long productId, ProductChangeStatus status);
}
