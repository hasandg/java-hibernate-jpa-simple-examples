package com.hasandag.ecommerce.repository.specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FieldMappingConfig<T> {

  private final Map<String, FieldMapping<T>> mappings = new HashMap<>();
  private final Map<String, Class<?>> entityFieldTypes;
  private final Map<String, RelationInfo> relationFields;
  private boolean distinct = false;

  public FieldMappingConfig(Class<T> entityClass) {
    this.entityFieldTypes = collectFieldTypes(entityClass);
    this.relationFields = collectRelationFields(entityClass);
  }

  static Map<String, Class<?>> collectFieldTypes(Class<?> clazz) {
    Map<String, Class<?>> types = new HashMap<>();
    for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
      for (Field field : c.getDeclaredFields()) {
        types.put(field.getName(), field.getType());
      }
    }
    return types;
  }

  private static Map<String, RelationInfo> collectRelationFields(Class<?> clazz) {
    Map<String, RelationInfo> relations = new HashMap<>();
    for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
      for (Field field : c.getDeclaredFields()) {
        Class<?> type = field.getType();
        if (isSingleValuedRelation(type)) {
          relations.put(field.getName(), new RelationInfo(type));
        }
      }
    }
    return relations;
  }

  private static boolean isSingleValuedRelation(Class<?> type) {
    return !Collection.class.isAssignableFrom(type)
        && !Map.class.isAssignableFrom(type)
        && !type.isPrimitive()
        && !type.equals(String.class)
        && !Number.class.isAssignableFrom(type)
        && !type.equals(Boolean.class)
        && type.isAnnotationPresent(jakarta.persistence.Entity.class);
  }

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

  public boolean isDistinct() {
    return distinct;
  }

  public FieldMapping<T> resolveFieldMapping(String filterKey) {
    FieldMapping<T> explicit = mappings.get(filterKey);
    if (explicit != null) {
      return explicit;
    }

    if (entityFieldTypes.containsKey(filterKey)) {
      return resolveDirectField(filterKey);
    }

    FieldMapping<T> rangeMapping = resolveRangeMapping(filterKey);
    if (rangeMapping != null) {
      return rangeMapping;
    }

    FieldMapping<T> relationIdMapping = resolveRelationIdMapping(filterKey);
    if (relationIdMapping != null) {
      return relationIdMapping;
    }

    return resolveRelationFieldMapping(filterKey);
  }

  private FieldMapping<T> resolveDirectField(String fieldName) {
    Class<?> fieldType = entityFieldTypes.get(fieldName);
    if (fieldType != null && fieldType.isEnum()) {
      return new FieldMapping<T>(fieldName)
          .caseSensitive()
          .withAdvancedPredicate(ctx -> ctx.autoEnumEquals(fieldType));
    }
    return new FieldMapping<>(fieldName);
  }

  private FieldMapping<T> resolveRangeMapping(String filterKey) {
    RangeKey rangeKey = RangeKey.parse(filterKey);
    if (rangeKey == null || !entityFieldTypes.containsKey(rangeKey.entityField())) {
      return null;
    }

    Class<?> fieldType = entityFieldTypes.get(rangeKey.entityField());
    Function<PredicateContext, Predicate> predicateFactory =
        resolveRangePredicate(fieldType, rangeKey.isMin());

    if (predicateFactory == null) {
      return null;
    }

    return new FieldMapping<T>(rangeKey.entityField()).withAdvancedPredicate(predicateFactory);
  }

  private Function<PredicateContext, Predicate> resolveRangePredicate(
      Class<?> fieldType, boolean isMin) {
    if (BigDecimal.class.equals(fieldType)) {
      return isMin
          ? PredicateContext::greaterThanOrEqualBigDecimal
          : PredicateContext::lessThanOrEqualBigDecimal;
    }
    if (Integer.class.equals(fieldType) || int.class.equals(fieldType)) {
      return isMin
          ? PredicateContext::greaterThanOrEqualInteger
          : PredicateContext::lessThanOrEqualInteger;
    }
    if (Long.class.equals(fieldType) || long.class.equals(fieldType)) {
      return isMin
          ? PredicateContext::greaterThanOrEqualLong
          : PredicateContext::lessThanOrEqualLong;
    }
    if (LocalDateTime.class.equals(fieldType) || LocalDate.class.equals(fieldType)) {
      return isMin
          ? PredicateContext::greaterThanOrEqualDateTime
          : PredicateContext::lessThanOrEqualDateTime;
    }
    return null;
  }

  private FieldMapping<T> resolveRelationIdMapping(String filterKey) {
    if (!filterKey.endsWith("Id") || filterKey.length() <= 2) {
      return null;
    }
    String relationName = filterKey.substring(0, filterKey.length() - 2);
    if (!relationFields.containsKey(relationName)) {
      return null;
    }
    return new FieldMapping<T>("id")
        .withJoin(relationName, JoinType.INNER)
        .withAdvancedPredicate(PredicateContext::equalLong);
  }

  private FieldMapping<T> resolveRelationFieldMapping(String filterKey) {
    for (Map.Entry<String, RelationInfo> entry : relationFields.entrySet()) {
      Map<String, Class<?>> relatedFieldTypes = collectFieldTypes(entry.getValue().targetType());
      if (relatedFieldTypes.containsKey(filterKey)) {
        return new FieldMapping<T>(filterKey).withJoin(entry.getKey(), JoinType.LEFT);
      }
    }
    return null;
  }

  private record RelationInfo(Class<?> targetType) {}

  private record RangeKey(String entityField, boolean isMin) {
    static RangeKey parse(String filterKey) {
      if (filterKey.startsWith("min") && filterKey.length() > 3) {
        return new RangeKey(
            Character.toLowerCase(filterKey.charAt(3)) + filterKey.substring(4), true);
      }
      if (filterKey.startsWith("max") && filterKey.length() > 3) {
        return new RangeKey(
            Character.toLowerCase(filterKey.charAt(3)) + filterKey.substring(4), false);
      }
      if (filterKey.endsWith("From") && filterKey.length() > 4) {
        return new RangeKey(filterKey.substring(0, filterKey.length() - 4), true);
      }
      if (filterKey.endsWith("To") && filterKey.length() > 2) {
        return new RangeKey(filterKey.substring(0, filterKey.length() - 2), false);
      }
      return null;
    }
  }
}
