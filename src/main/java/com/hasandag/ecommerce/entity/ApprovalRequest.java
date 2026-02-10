package com.hasandag.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "approval_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "entity_type", nullable = false)
  private String entityType; // E.g., "PRODUCT", "CATEGORY"

  @Column(name = "action_type", nullable = false)
  private String actionType; // E.g., "CREATE", "UPDATE", "DELETE"

  @Column(name = "entity_id")
  private Long entityId; // Null for CREATE

  @Column(name = "dto_class", nullable = false)
  private String dtoClass; // Fully qualified class name of the DTO

  @Column(columnDefinition = "TEXT", nullable = false)
  private String payload; // JSON representation of the DTO

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ChangeRequestStatus status;

  @Column(name = "request_date", nullable = false)
  private LocalDateTime requestDate;

  @Column(name = "requested_by")
  private String requestedBy;

  @Column(name = "rejection_reason")
  private String rejectionReason;
}
