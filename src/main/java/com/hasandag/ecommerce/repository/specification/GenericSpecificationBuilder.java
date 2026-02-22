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
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;

public class GenericSpecificationBuilder {

  private static final Set<String> IGNORED_FILTER_KEYS =
      Set.of("page", "size", "groupOperator", "filterGroups", "filters");

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

      if (IGNORED_FILTER_KEYS.contains(fieldName)) {
        continue;
      }

      if (value == null || (value instanceof String s && s.trim().isEmpty())) {
        continue;
      }

      FieldMapping<T> mapping = config.resolveFieldMapping(fieldName);
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
    private final Map<String, Class<?>> entityFieldTypes;
    private final Map<String, RelationInfo> relationFields;
    private boolean distinct = false;

    public FieldMappingConfig(Class<T> entityClass) {
      this.entityFieldTypes = collectFieldTypes(entityClass);
      this.relationFields = collectRelationFields(entityClass);
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

    private static Map<String, Class<?>> collectFieldTypes(Class<?> clazz) {
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
          if (!Collection.class.isAssignableFrom(type)
              && !Map.class.isAssignableFrom(type)
              && !type.isPrimitive()
              && !type.equals(String.class)
              && !Number.class.isAssignableFrom(type)
              && !type.equals(Boolean.class)
              && type.isAnnotationPresent(jakarta.persistence.Entity.class)) {
            relations.put(field.getName(), new RelationInfo(type));
          }
        }
      }
      return relations;
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
      String entityField = null;
      boolean isMin = false;

      if (filterKey.startsWith("min") && filterKey.length() > 3) {
        entityField = Character.toLowerCase(filterKey.charAt(3)) + filterKey.substring(4);
        isMin = true;
      } else if (filterKey.startsWith("max") && filterKey.length() > 3) {
        entityField = Character.toLowerCase(filterKey.charAt(3)) + filterKey.substring(4);
        isMin = false;
      } else if (filterKey.endsWith("From") && filterKey.length() > 4) {
        entityField = filterKey.substring(0, filterKey.length() - 4);
        isMin = true;
      } else if (filterKey.endsWith("To") && filterKey.length() > 2) {
        entityField = filterKey.substring(0, filterKey.length() - 2);
        isMin = false;
      }

      if (entityField == null || !entityFieldTypes.containsKey(entityField)) {
        return null;
      }

      Class<?> fieldType = entityFieldTypes.get(entityField);
      Function<PredicateContext, Predicate> predicateFactory =
          resolveRangePredicate(fieldType, isMin);

      if (predicateFactory == null) {
        return null;
      }

      return new FieldMapping<T>(entityField).withAdvancedPredicate(predicateFactory);
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

    public boolean isDistinct() {
      return distinct;
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

    private static Integer parseInteger(Object val) {
      if (val == null) return null;
      if (val instanceof Integer i) return i;
      if (val instanceof Number n) return n.intValue();
      try {
        return Integer.parseInt(val.toString().trim());
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

    @SuppressWarnings("unchecked")
    public Predicate greaterThanOrEqualLong() {
      Long parsed = parseLong(value);
      return parsed != null
          ? criteriaBuilder.greaterThanOrEqualTo((Path<Long>) fieldPath, parsed)
          : null;
    }

    @SuppressWarnings("unchecked")
    public Predicate lessThanOrEqualLong() {
      Long parsed = parseLong(value);
      return parsed != null
          ? criteriaBuilder.lessThanOrEqualTo((Path<Long>) fieldPath, parsed)
          : null;
    }

    @SuppressWarnings("unchecked")
    public Predicate greaterThanOrEqualInteger() {
      Integer parsed = parseInteger(value);
      return parsed != null
          ? criteriaBuilder.greaterThanOrEqualTo((Path<Integer>) fieldPath, parsed)
          : null;
    }

    @SuppressWarnings("unchecked")
    public Predicate lessThanOrEqualInteger() {
      Integer parsed = parseInteger(value);
      return parsed != null
          ? criteriaBuilder.lessThanOrEqualTo((Path<Integer>) fieldPath, parsed)
          : null;
    }
  }
}
