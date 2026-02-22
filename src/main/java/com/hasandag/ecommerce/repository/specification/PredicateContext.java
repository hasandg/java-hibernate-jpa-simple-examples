package com.hasandag.ecommerce.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class PredicateContext {

  private final Path<?> fieldPath;
  private final Object value;
  private final CriteriaBuilder criteriaBuilder;
  private final Root<?> root;
  private final CriteriaQuery<?> query;

  public PredicateContext(
      Path<?> fieldPath,
      Object value,
      CriteriaBuilder criteriaBuilder,
      Root<?> root,
      CriteriaQuery<?> query) {
    this.fieldPath = fieldPath;
    this.value = value;
    this.criteriaBuilder = criteriaBuilder;
    this.root = root;
    this.query = query;
  }

  public static Integer parseInteger(Object val) {
    if (val == null) return null;
    if (val instanceof Integer i) return i;
    if (val instanceof Number n) return n.intValue();
    try {
      return Integer.parseInt(val.toString().trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static BigDecimal parseBigDecimal(Object val) {
    if (val == null) return null;
    if (val instanceof BigDecimal bd) return bd;
    if (val instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
    try {
      return new BigDecimal(val.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static LocalDateTime parseDateTime(Object val) {
    if (val == null) return null;
    try {
      return LocalDateTime.parse(val.toString().trim());
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  private static Long parseLong(Object val) {
    if (val == null) return null;
    if (val instanceof Long l) return l;
    if (val instanceof Number n) return n.longValue();
    try {
      return Long.parseLong(val.toString().trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public Path<?> getFieldPath() {
    return fieldPath;
  }

  public Object getValue() {
    return value;
  }

  public CriteriaBuilder getCriteriaBuilder() {
    return criteriaBuilder;
  }

  public Root<?> getRoot() {
    return root;
  }

  public CriteriaQuery<?> getQuery() {
    return query;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public Predicate autoEnumEquals(Class<?> enumType) {
    String raw = value != null ? value.toString().trim() : null;
    if (raw == null) return null;
    try {
      Enum enumValue = Enum.valueOf((Class<Enum>) enumType, raw.toUpperCase());
      return criteriaBuilder.equal(fieldPath, enumValue);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public Predicate greaterThanOrEqualBigDecimal() {
    return compareComparable(parseBigDecimal(value), true);
  }

  public Predicate lessThanOrEqualBigDecimal() {
    return compareComparable(parseBigDecimal(value), false);
  }

  public Predicate greaterThanOrEqualDateTime() {
    return compareComparable(parseDateTime(value), true);
  }

  public Predicate lessThanOrEqualDateTime() {
    return compareComparable(parseDateTime(value), false);
  }

  public Predicate equalLong() {
    Long parsed = parseLong(value);
    return parsed != null ? criteriaBuilder.equal(fieldPath, parsed) : null;
  }

  public Predicate greaterThanOrEqualLong() {
    return compareComparable(parseLong(value), true);
  }

  public Predicate lessThanOrEqualLong() {
    return compareComparable(parseLong(value), false);
  }

  public Predicate greaterThanOrEqualInteger() {
    return compareComparable(parseInteger(value), true);
  }

  public Predicate lessThanOrEqualInteger() {
    return compareComparable(parseInteger(value), false);
  }

  @SuppressWarnings("unchecked")
  private <Y extends Comparable<Y>> Predicate compareComparable(Y parsed, boolean greaterThan) {
    if (parsed == null) return null;
    Path<Y> typedPath = (Path<Y>) fieldPath;
    return greaterThan
        ? criteriaBuilder.greaterThanOrEqualTo(typedPath, parsed)
        : criteriaBuilder.lessThanOrEqualTo(typedPath, parsed);
  }
}
