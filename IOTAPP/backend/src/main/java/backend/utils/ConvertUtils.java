package backend.utils;

import lombok.Data;
import java.lang.reflect.Field;
import java.util.*;

public class ConvertUtils{

    @Data
    public static class ChildEntity {
        private String childName;
        private Integer childValue;
    }

    @Data
    public static class EntityA {
        private String aaa;
        private Integer bbb;
        private List<ChildEntity> childrenList;
        private Set<ChildEntity> childrenSet;
        private Map<String, ChildEntity> childrenMap;
    }

    @Data
    public static class TargetModel {
        private String aaa;
        private Integer bbb;
        private List<ChildEntity> childrenList;
        private Set<ChildEntity> childrenSet;
        private Map<String, ChildEntity> childrenMap;
    }



        public static <T> T convertToFilteredModel(Class<T> targetClass, Object... sources) {
            if (sources == null || sources.length == 0) {
                return null;
            }

            try {
                // 检查 targetClass 是否为空
                if (targetClass == null) {
                    throw new IllegalArgumentException("目标类不能为空");
                }

                // 创建目标类的新实例
                T target = targetClass.getDeclaredConstructor().newInstance();
                Field[] targetFields = targetClass.getDeclaredFields();
                Map<String, Field> targetFieldMap = new HashMap<>();
                for (Field targetField : targetFields) {
                    targetField.setAccessible(true);
                    targetFieldMap.put(targetField.getName(), targetField);
                }

                for (Object source : sources) {
                    if (source == null) continue;

                    Field[] sourceFields = source.getClass().getDeclaredFields();
                    for (Field sourceField : sourceFields) {
                        sourceField.setAccessible(true);
                        Object value = sourceField.get(source);

                        if (value != null) { // Filter out null values
                            Field targetField = targetFieldMap.get(sourceField.getName());
                            if (targetField != null && targetField.getType().equals(sourceField.getType())) {
                                if (Collection.class.isAssignableFrom(sourceField.getType())) {
                                    Collection<?> collection = (Collection<?>) value;
                                    Collection<Object> targetCollection = createCollectionInstance(sourceField.getType());
                                    for (Object item : collection) {
                                        if (item != null && !item.getClass().isPrimitive()) {
                                            Object targetItem = convertToFilteredModel(item.getClass(), item); // 递归转换子元素
                                            targetCollection.add(targetItem);
                                        } else {
                                            targetCollection.add(item);
                                        }
                                    }
                                    targetField.set(target, targetCollection);
                                } else if (Map.class.isAssignableFrom(sourceField.getType())) {
                                    Map<?, ?> map = (Map<?, ?>) value;
                                    Map<Object, Object> targetMap = createMapInstance();
                                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                                        Object key = entry.getKey();
                                        Object val = entry.getValue();
                                        if (val != null && !val.getClass().isPrimitive()) {
                                            val = convertToFilteredModel(val.getClass(), val); // 递归转换子元素
                                        }
                                        targetMap.put(key, val);
                                    }
                                    targetField.set(target, targetMap);
                                } else {
                                    targetField.set(target, value);
                                }
                            }
                        }
                    }
                }
                return target;
            } catch (Exception e) {
                throw new RuntimeException("Error during conversion", e);
            }
        }

        private static <E> Collection<E> createCollectionInstance(Class<?> collectionType) {
            if (List.class.isAssignableFrom(collectionType)) {
                return new ArrayList<>();
            } else if (Set.class.isAssignableFrom(collectionType)) {
                return new HashSet<>();
            }
            return new ArrayList<>();
        }

        private static Map<Object, Object> createMapInstance() {
            return new HashMap<>();
        }

    public static void main(String[] args) {
        try {
            // Create sample data
            EntityA entityA = new EntityA();
            entityA.setAaa("1");

            ChildEntity childA = new ChildEntity();
            childA.setChildName("childA");
            childA.setChildValue(100);

            entityA.setChildrenList(Arrays.asList(childA));
            entityA.setChildrenSet(new HashSet<>(Arrays.asList(childA)));
            entityA.setChildrenMap(Map.of("child1", childA));

            // Convert and print the result
            TargetModel targetModel = ConvertUtils.convertToFilteredModel(TargetModel.class, entityA);
            System.out.println("Converted Target Model: " + targetModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
