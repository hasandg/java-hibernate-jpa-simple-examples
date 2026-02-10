package com.hasandag.ecommerce.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hasandag.ecommerce.dto.ProductCreateDTO;
import com.hasandag.ecommerce.dto.ProductUpdateDTO;
import com.hasandag.ecommerce.entity.ApprovalRequest;
import com.hasandag.ecommerce.entity.ChangeRequestStatus;
import com.hasandag.ecommerce.repository.ApprovalRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenericApprovalService {

  private final ApprovalRequestRepository approvalRequestRepository;
  private final ObjectMapper objectMapper;
  private final ProductService productService;

  // Can add other services here (e.g. CategoryService)

  @Transactional
  public Long submitRequest(
      String entityType, String actionType, Long entityId, Object dto, String requestedBy) {
    try {
      ApprovalRequest request = new ApprovalRequest();
      request.setEntityType(entityType);
      request.setActionType(actionType);
      request.setEntityId(entityId);
      request.setDtoClass(dto.getClass().getName());
      request.setPayload(objectMapper.writeValueAsString(dto));
      request.setStatus(ChangeRequestStatus.PENDING);
      request.setRequestDate(LocalDateTime.now());
      request.setRequestedBy(requestedBy);

      return approvalRequestRepository.save(request).getId();
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error serializing approval request payload", e);
    }
  }

  @Transactional(readOnly = true)
  public List<ApprovalRequest> getPendingRequests() {
    return approvalRequestRepository.findByStatus(ChangeRequestStatus.PENDING);
  }

  @Transactional
  public void approveRequest(Long requestId) {
    ApprovalRequest request =
        approvalRequestRepository
            .findById(requestId)
            .orElseThrow(
                () -> new EntityNotFoundException("Approval request not found: " + requestId));

    if (request.getStatus() != ChangeRequestStatus.PENDING) {
      throw new IllegalStateException("Request is not in PENDING status");
    }

    try {
      Class<?> dtoClass = Class.forName(request.getDtoClass());
      Object dto = objectMapper.readValue(request.getPayload(), dtoClass);

      applyChange(request.getEntityType(), request.getActionType(), dto);

      request.setStatus(ChangeRequestStatus.APPROVED);
      approvalRequestRepository.save(request);
    } catch (ClassNotFoundException | JsonProcessingException e) {
      throw new RuntimeException("Error processing approval request", e);
    }
  }

  @Transactional
  public void rejectRequest(Long requestId, String reason) {
    ApprovalRequest request =
        approvalRequestRepository
            .findById(requestId)
            .orElseThrow(
                () -> new EntityNotFoundException("Approval request not found: " + requestId));

    if (request.getStatus() != ChangeRequestStatus.PENDING) {
      throw new IllegalStateException("Request is not in PENDING status");
    }

    request.setStatus(ChangeRequestStatus.REJECTED);
    request.setRejectionReason(reason);
    approvalRequestRepository.save(request);
  }

  private void applyChange(String entityType, String actionType, Object dto) {
    switch (entityType) {
      case "PRODUCT":
        handleProductChange(actionType, dto);
        break;
      // Add other entity types here
      default:
        throw new IllegalArgumentException("Unknown entity type: " + entityType);
    }
  }

  private void handleProductChange(String actionType, Object dto) {
    switch (actionType) {
      case "CREATE":
        productService.create((ProductCreateDTO) dto);
        break;
      case "UPDATE":
        productService.update((ProductUpdateDTO) dto);
        break;
      // DELETE case if needed
      default:
        throw new IllegalArgumentException("Unknown action type: " + actionType);
    }
  }
}
