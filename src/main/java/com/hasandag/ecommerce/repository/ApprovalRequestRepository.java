package com.hasandag.ecommerce.repository;

import com.hasandag.ecommerce.entity.ApprovalRequest;
import com.hasandag.ecommerce.entity.ChangeRequestStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {
  List<ApprovalRequest> findByStatus(ChangeRequestStatus status);

  List<ApprovalRequest> findByEntityTypeAndStatus(String entityType, ChangeRequestStatus status);
}
