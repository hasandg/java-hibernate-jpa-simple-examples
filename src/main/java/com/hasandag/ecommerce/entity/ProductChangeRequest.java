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
  @JoinColumn(name = "product_id")
  private Product product;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ChangeRequestType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ChangeRequestStatus status;

  @Column(nullable = false)
  private String proposedName;

  @Column(nullable = false)
  private String proposedDescription;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal proposedPrice;

  @Column(nullable = false)
  private Integer proposedStock;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "proposed_category_id")
  private Category proposedCategory;

  @Column(nullable = false)
  private String requestedBy;

  private String reviewedBy;

  private String rejectionReason;

  @Column(nullable = false)
  private LocalDateTime requestedAt;

  private LocalDateTime reviewedAt;
}
