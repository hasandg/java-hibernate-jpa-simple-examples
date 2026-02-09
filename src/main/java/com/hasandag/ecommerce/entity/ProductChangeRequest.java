package com.hasandag.ecommerce.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_change_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductChangeRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "proposed_name")
  private String proposedName;

  @Column(name = "proposed_description", length = 1000)
  private String proposedDescription;

  @Column(name = "proposed_price", precision = 10, scale = 2)
  private BigDecimal proposedPrice;

  @Column(name = "proposed_stock")
  private Integer proposedStock;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProductChangeStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "moderator_id")
  private User moderator;

  @Column(name = "moderator_comments", length = 500)
  private String moderatorComments;

  @Column(name = "reviewed_at")
  private LocalDateTime reviewedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (status == null) {
      status = ProductChangeStatus.PENDING;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
