package com.hasandag.ecommerce.repository;

import com.hasandag.ecommerce.entity.ChangeRequestStatus;
import com.hasandag.ecommerce.entity.ProductChangeRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductChangeRequestRepository
    extends JpaRepository<ProductChangeRequest, Long>,
        JpaSpecificationExecutor<ProductChangeRequest> {

  List<ProductChangeRequest> findByStatus(ChangeRequestStatus status);

  List<ProductChangeRequest> findByProductId(Long productId);

  List<ProductChangeRequest> findByRequestedBy(String requestedBy);

  List<ProductChangeRequest> findByProductIdAndStatus(Long productId, ChangeRequestStatus status);
}
