package com.hasandag.ecommerce.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_drafts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDraft {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private Long version;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, length = 1000)
  private String description;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(nullable = false)
  private Integer stock;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private ProductDraftStatus status;

  @Column(nullable = false, length = 128)
  private String submittedBy;

  @Column(nullable = false)
  private LocalDateTime submittedAt;

  @Column(length = 128)
  private String reviewedBy;

  private LocalDateTime reviewedAt;

  @Column(length = 1000)
  private String rejectionReason;
}
