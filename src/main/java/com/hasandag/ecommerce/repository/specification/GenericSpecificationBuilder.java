package com.hasandag.ecommerce.repository.specification;

import com.hasandag.ecommerce.dto.FilterDTO;
import com.hasandag.ecommerce.dto.FilterGroup;
import com.hasandag.ecommerce.dto.LogicalOperator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;

public final class GenericSpecificationBuilder {

  private static final Set<String> IGNORED_FILTER_KEYS =
      Set.of("page", "size", "groupOperator", "filterGroups", "filters");

  private GenericSpecificationBuilder() {}

  public static <T> Specification<T> buildSpecification(FilterDTO filterDTO, Class<T> entityClass) {
    return buildSpecification(filterDTO, new FieldMappingConfig<>(entityClass));
  }

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

      return combinePredicates(cb, groupPredicates, filterDTO.getGroupOperator());
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
      Predicate predicate = buildFilterPredicate(entry, config, root, query, cb, joins);
      if (predicate != null) {
        predicates.add(predicate);
      }
    }

    if (predicates.isEmpty()) {
      return null;
    }

    return combinePredicates(cb, predicates, group.getOperator());
  }

  private static <T> Predicate buildFilterPredicate(
      Map.Entry<String, Object> entry,
      FieldMappingConfig<T> config,
      Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder cb,
      Map<String, Join<?, ?>> joins) {

    String fieldName = entry.getKey();
    Object value = entry.getValue();

    if (IGNORED_FILTER_KEYS.contains(fieldName)) {
      return null;
    }
    if (value == null || (value instanceof String s && s.trim().isEmpty())) {
      return null;
    }

    FieldMapping<T> mapping = config.resolveFieldMapping(fieldName);
    if (mapping == null) {
      return null;
    }

    Path<?> fieldPath = resolveFieldPath(root, mapping, joins);
    if (fieldPath == null) {
      return null;
    }

    return buildPredicate(fieldPath, value, mapping, cb, root, query);
  }

  @SuppressWarnings("unchecked")
  private static <T> Path<?> resolveFieldPath(
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

  private static <T> Predicate buildPredicate(
      Path<?> fieldPath,
      Object value,
      FieldMapping<?> mapping,
      CriteriaBuilder cb,
      Root<T> root,
      CriteriaQuery<?> query) {
    if (mapping.getAdvancedPredicateBuilder() != null) {
      return mapping
          .getAdvancedPredicateBuilder()
          .apply(new PredicateContext(fieldPath, value, cb, root, query));
    }
    if (mapping.getPredicateBuilder() != null) {
      return mapping.getPredicateBuilder().apply(fieldPath, value);
    }

    return buildDefaultPredicate(fieldPath, value, mapping, cb);
  }

  @SuppressWarnings("unchecked")
  private static Predicate buildDefaultPredicate(
      Path<?> fieldPath, Object value, FieldMapping<?> mapping, CriteriaBuilder cb) {
    Class<?> fieldType = fieldPath.getJavaType();

    if (String.class.equals(fieldType)) {
      return buildStringPredicate((Path<String>) fieldPath, value.toString().trim(), mapping, cb);
    }
    if (isNumericType(fieldType) && value instanceof Number) {
      return cb.equal(fieldPath, value);
    }
    if (Boolean.class.equals(fieldType) || boolean.class.equals(fieldType)) {
      Boolean boolValue = value instanceof Boolean b ? b : Boolean.parseBoolean(value.toString());
      return cb.equal(fieldPath, boolValue);
    }

    return cb.equal(fieldPath, value);
  }

  private static Predicate buildStringPredicate(
      Path<String> fieldPath, String value, FieldMapping<?> mapping, CriteriaBuilder cb) {
    if (mapping.isCaseInsensitive()) {
      return cb.like(cb.lower(fieldPath), "%" + value.toLowerCase() + "%");
    }
    return cb.like(fieldPath, "%" + value + "%");
  }

  private static boolean isNumericType(Class<?> type) {
    return Number.class.isAssignableFrom(type) || (type.isPrimitive() && type != boolean.class);
  }

  private static Predicate combinePredicates(
      CriteriaBuilder cb, List<Predicate> predicates, LogicalOperator operator) {
    Predicate[] array = predicates.toArray(new Predicate[0]);
    return operator == LogicalOperator.OR ? cb.or(array) : cb.and(array);
  }
}
