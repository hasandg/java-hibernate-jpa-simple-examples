package com.hasandag.ecommerce.repository.specification;

import com.hasandag.ecommerce.dto.FilterDTO;
import com.hasandag.ecommerce.dto.FilterGroup;
import com.hasandag.ecommerce.dto.LogicalOperator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;

public class GenericSpecificationBuilder {

  public static <T> Specification<T> buildSpecification(
      FilterDTO filterDTO, FieldMappingConfig<T> config) {
    List<FilterGroup> groups = filterDTO.getResolvedFilterGroups();
    if (groups.isEmpty()) {
      return (root, query, cb) -> cb.conjunction();
    }

    return (root, query, cb) -> {
      Map<String, Join<?, ?>> joins = new HashMap<>();
      List<Predicate> groupPredicates = new ArrayList<>();

      for (FilterGroup group : groups) {
        Predicate groupPredicate = buildGroupPredicate(group, config, root, query, cb, joins);
        if (groupPredicate != null) {
          groupPredicates.add(groupPredicate);
        }
      }

      if (groupPredicates.isEmpty()) {
        return cb.conjunction();
      }

      if (config.isDistinct()) {
        query.distinct(true);
      }

      Predicate[] predicateArray = groupPredicates.toArray(new Predicate[0]);
      return filterDTO.getGroupOperator() == LogicalOperator.OR
          ? cb.or(predicateArray)
          : cb.and(predicateArray);
    };
  }

  private static <T> Predicate buildGroupPredicate(
      FilterGroup group,
      FieldMappingConfig<T> config,
      Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder cb,
      Map<String, Join<?, ?>> joins) {

    Map<String, Object> filters = group.getFilters();
    if (filters.isEmpty()) {
      return null;
    }

    List<Predicate> predicates = new ArrayList<>();

    for (Map.Entry<String, Object> entry : filters.entrySet()) {
      String fieldName = entry.getKey();
      Object value = entry.getValue();

      if (value == null || (value instanceof String s && s.trim().isEmpty())) {
        continue;
      }

      FieldMapping<T> mapping = config.getFieldMapping(fieldName);
      if (mapping == null) {
        continue;
      }

      Path<?> fieldPath = getFieldPath(root, mapping, joins);
      if (fieldPath == null) {
        continue;
      }

      Predicate predicate = buildPredicate(fieldPath, value, mapping, cb, root, query);
      if (predicate != null) {
        predicates.add(predicate);
      }
    }

    if (predicates.isEmpty()) {
      return null;
    }

    Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
    return group.getOperator() == LogicalOperator.OR
        ? cb.or(predicateArray)
        : cb.and(predicateArray);
  }

  @SuppressWarnings("unchecked")
  private static <T> Path<?> getFieldPath(
      Root<T> root, FieldMapping<T> mapping, Map<String, Join<?, ?>> joins) {
    if (mapping.getJoinPath() != null) {
      Join<Object, Object> join =
          (Join<Object, Object>)
              joins.computeIfAbsent(
                  mapping.getJoinPath(), k -> root.join(k, mapping.getJoinType()));
      return join.get(mapping.getFieldName());
    }
    return root.get(mapping.getFieldName());
  }

  @SuppressWarnings("unchecked")
  private static <T> Predicate buildPredicate(
      Path<?> fieldPath,
      Object value,
      FieldMapping<?> mapping,
      CriteriaBuilder cb,
      Root<T> root,
      CriteriaQuery<?> query) {
    if (mapping.getAdvancedPredicateBuilder() != null) {
      PredicateContext context = new PredicateContext(fieldPath, value, cb, root, query);
      return mapping.getAdvancedPredicateBuilder().apply(context);
    }
    if (mapping.getPredicateBuilder() != null) {
      return mapping.getPredicateBuilder().apply(fieldPath, value);
    }

    Class<?> fieldType = fieldPath.getJavaType();

    if (String.class.equals(fieldType)) {
      String stringValue = value.toString().trim();
      if (mapping.isCaseInsensitive()) {
        return cb.like(cb.lower((Path<String>) fieldPath), "%" + stringValue.toLowerCase() + "%");
      } else {
        return cb.like((Path<String>) fieldPath, "%" + stringValue + "%");
      }
    } else if (Number.class.isAssignableFrom(fieldType)
        || (fieldType.isPrimitive() && fieldType != boolean.class && fieldType != Boolean.TYPE)) {
      if (value instanceof Number) {
        return cb.equal(fieldPath, value);
      }
    } else if (Boolean.class.equals(fieldType)
        || boolean.class.equals(fieldType)
        || Boolean.TYPE.equals(fieldType)) {
      Boolean boolValue = value instanceof Boolean b ? b : Boolean.parseBoolean(value.toString());
      return cb.equal(fieldPath, boolValue);
    }

    return cb.equal(fieldPath, value);
  }

  public static class FieldMapping<T> {
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

  public static class FieldMappingConfig<T> {
    private final Map<String, FieldMapping<T>> mappings = new HashMap<>();
    private boolean distinct = false;

    public FieldMappingConfig<T> addMapping(String filterKey, String fieldName) {
      mappings.put(filterKey, new FieldMapping<>(fieldName));
      return this;
    }

    public FieldMappingConfig<T> addMapping(String filterKey, FieldMapping<T> mapping) {
      mappings.put(filterKey, mapping);
      return this;
    }

    public FieldMappingConfig<T> withDistinct() {
      this.distinct = true;
      return this;
    }

    public FieldMapping<T> getFieldMapping(String filterKey) {
      return mappings.get(filterKey);
    }

    public boolean isDistinct() {
      return distinct;
    }
  }

  public static class PredicateContext {
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
      } catch (Exception e) {
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

    public <E extends Enum<E>> Predicate enumEquals(Class<E> enumType) {
      String raw = value != null ? value.toString().trim() : null;
      if (raw == null) return null;
      try {
        E enumValue = Enum.valueOf(enumType, raw.toUpperCase());
        return criteriaBuilder.equal(fieldPath, enumValue);
      } catch (IllegalArgumentException e) {
        return null;
      }
    }

    @SuppressWarnings("unchecked")
    public Predicate greaterThanOrEqualBigDecimal() {
      BigDecimal parsed = parseBigDecimal(value);
      return parsed != null
          ? criteriaBuilder.greaterThanOrEqualTo((Path<BigDecimal>) fieldPath, parsed)
          : null;
    }

    @SuppressWarnings("unchecked")
    public Predicate lessThanOrEqualBigDecimal() {
      BigDecimal parsed = parseBigDecimal(value);
      return parsed != null
          ? criteriaBuilder.lessThanOrEqualTo((Path<BigDecimal>) fieldPath, parsed)
          : null;
    }

    @SuppressWarnings("unchecked")
    public Predicate greaterThanOrEqualDateTime() {
      LocalDateTime parsed = parseDateTime(value);
      return parsed != null
          ? criteriaBuilder.greaterThanOrEqualTo((Path<LocalDateTime>) fieldPath, parsed)
          : null;
    }

    @SuppressWarnings("unchecked")
    public Predicate lessThanOrEqualDateTime() {
      LocalDateTime parsed = parseDateTime(value);
      return parsed != null
          ? criteriaBuilder.lessThanOrEqualTo((Path<LocalDateTime>) fieldPath, parsed)
          : null;
    }

    public Predicate equalLong() {
      Long parsed = parseLong(value);
      return parsed != null ? criteriaBuilder.equal(fieldPath, parsed) : null;
    }
  }
}
