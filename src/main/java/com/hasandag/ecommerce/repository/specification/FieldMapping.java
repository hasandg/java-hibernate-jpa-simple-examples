package com.hasandag.ecommerce.repository.specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FieldMapping<T> {

  private final String fieldName;
  private String joinPath;
  private JoinType joinType = JoinType.INNER;
  private boolean caseInsensitive = true;
  private BiFunction<Path<?>, Object, Predicate> predicateBuilder;
  private Function<PredicateContext, Predicate> advancedPredicateBuilder;

  public FieldMapping(String fieldName) {
    this.fieldName = fieldName;
  }

  public FieldMapping<T> withJoin(String joinPath, JoinType joinType) {
    this.joinPath = joinPath;
    this.joinType = joinType;
    return this;
  }

  public FieldMapping<T> caseSensitive() {
    this.caseInsensitive = false;
    return this;
  }

  public FieldMapping<T> withCustomPredicate(
      BiFunction<Path<?>, Object, Predicate> predicateBuilder) {
    this.predicateBuilder = predicateBuilder;
    return this;
  }

  public FieldMapping<T> withAdvancedPredicate(
      Function<PredicateContext, Predicate> predicateBuilder) {
    this.advancedPredicateBuilder = predicateBuilder;
    return this;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getJoinPath() {
    return joinPath;
  }

  public JoinType getJoinType() {
    return joinType;
  }

  public boolean isCaseInsensitive() {
    return caseInsensitive;
  }

  public BiFunction<Path<?>, Object, Predicate> getPredicateBuilder() {
    return predicateBuilder;
  }

  public Function<PredicateContext, Predicate> getAdvancedPredicateBuilder() {
    return advancedPredicateBuilder;
  }
}
