package com.hasandag.ecommerce.repository.specification;

import com.hasandag.ecommerce.dto.FilterDTO;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
    if (filterDTO == null || filterDTO.getFilters() == null || filterDTO.getFilters().isEmpty()) {
      return (root, query, cb) -> cb.conjunction();
    }

    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      Map<String, Join<?, ?>> joins = new HashMap<>();

      for (Map.Entry<String, Object> entry : filterDTO.getFilters().entrySet()) {
        String fieldName = entry.getKey();
        Object value = entry.getValue();

        if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
          continue;
        }

        FieldMapping<T> mapping = config.getFieldMapping(fieldName);
        if (mapping == null) {
          continue;
        }

        Path<?> fieldPath = getFieldPath(root, mapping, joins, cb);
        if (fieldPath == null) {
          continue;
        }

        Predicate predicate = buildPredicate(fieldPath, value, mapping, cb, root, query);
        if (predicate != null) {
          predicates.add(predicate);
        }
      }

      if (predicates.isEmpty()) {
        return cb.conjunction();
      }

      if (config.isDistinct()) {
        query.distinct(true);
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  @SuppressWarnings("unchecked")
  private static <T> Path<?> getFieldPath(
      Root<T> root, FieldMapping<T> mapping, Map<String, Join<?, ?>> joins, CriteriaBuilder cb) {
    if (mapping.getJoinPath() != null) {
      Join<Object, Object> join =
          (Join<Object, Object>)
              joins.computeIfAbsent(
                  mapping.getJoinPath(), k -> root.join(k, mapping.getJoinType()));
      return join.get(mapping.getFieldName());
    }
    return root.get(mapping.getFieldName());
  }

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
      Boolean boolValue =
          value instanceof Boolean ? (Boolean) value : Boolean.parseBoolean(value.toString());
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
  }
}
