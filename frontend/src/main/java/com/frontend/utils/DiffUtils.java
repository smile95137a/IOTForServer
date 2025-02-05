package src.main.java.com.frontend.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DiffUtils {

	public static <T> DiffResult<T> compareObjects(T obj1, T obj2) throws IllegalAccessException {
		if (obj1 == null || obj2 == null) {
			throw new IllegalArgumentException("Objects must not be null");
		}
		if (obj1.getClass() != obj2.getClass()) {
			throw new IllegalArgumentException("Objects must be of the same type");
		}

		DiffBuilder<T> diffBuilder = new DiffBuilder<>(obj1, obj2, ToStringStyle.SHORT_PREFIX_STYLE);

		for (Field field : obj1.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			if (List.class.isAssignableFrom(field.getType())) {
				compareLists(field, obj1, obj2, diffBuilder);
			} else {
				diffBuilder.append(field.getName(), field.get(obj1), field.get(obj2));
			}
		}

		return diffBuilder.build();
	}

	private static <T> void compareLists(Field field, T obj1, T obj2, DiffBuilder<T> diffBuilder)
			throws IllegalAccessException {
		List<?> list1 = (List<?>) field.get(obj1);
		List<?> list2 = (List<?>) field.get(obj2);

		if (list1 == null || list2 == null || list1.size() != list2.size()) {
			diffBuilder.append(field.getName(), list1, list2);
		} else {
			for (int i = 0; i < list1.size(); i++) {
				Object item1 = list1.get(i);
				Object item2 = list2.get(i);

				if (!item1.equals(item2)) {
					diffBuilder.append(field.getName() + "[" + i + "]", item1, item2);
				}
			}
		}
	}

	public static <T> List<String> getDifferences(T obj1, T obj2) throws IllegalAccessException {
		DiffResult<T> differences = compareObjects(obj1, obj2);
		List<String> diffList = new ArrayList<>();

		for (Diff<?> diff : differences.getDiffs()) {
			diffList.add(diff.getFieldName() + " -> obj1: " + diff.getLeft() + ", obj2: " + diff.getRight());
		}

		return diffList;
	}

	public static <T> void applyDifferencesForFields(T source, T target, Set<String> fieldsToUpdate)
			throws IllegalAccessException {
		if (source == null || target == null) {
			throw new IllegalArgumentException("Objects must not be null");
		}
		if (source.getClass() != target.getClass()) {
			throw new IllegalArgumentException("Objects must be of the same type");
		}

		for (Field field : source.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			if (fieldsToUpdate.contains(field.getName())) {
				if (List.class.isAssignableFrom(field.getType())) {
					applyListDifferences(field, source, target);
				} else {
					Object sourceValue = field.get(source);
					Object targetValue = field.get(target);

					if (sourceValue != null && !sourceValue.equals(targetValue)) {
						field.set(target, sourceValue);
					}
				}
			}
		}
	}

	  private static <T> void applyListDifferences(Field field, T source, T target) throws IllegalAccessException {
	        List<?> sourceList = (List<?>) field.get(source);
	        List<?> targetList = (List<?>) field.get(target);

	        if (sourceList != null && targetList != null) {
	            for (int i = 0; i < sourceList.size(); i++) {
	                Object sourceItem = sourceList.get(i);
	                Object targetItem = targetList.get(i);

	                if (!sourceItem.equals(targetItem)) {
	                    if (sourceItem.getClass() == targetItem.getClass()) {
	                        applyDifferencesForFields(sourceItem, targetItem, getAllFieldNames(sourceItem.getClass()));
	                    } else {
	                        ((List<Object>) targetList).set(i, sourceItem);
	                    }
	                }
	            }
	        }
	    }

	private static Set<String> getAllFieldNames(Class<?> clazz) {
		Set<String> fieldNames = new HashSet<>();
		for (Field field : clazz.getDeclaredFields()) {
			fieldNames.add(field.getName());
		}
		return fieldNames;
	}

	public static void main(String[] args) throws IllegalAccessException {
		List<House> list1 = new ArrayList<>();
		list1.add(new House("123 Street", 1000));
		list1.add(new House("456 Avenue", 2000));

		List<House> list2 = new ArrayList<>();
		list2.add(new House("123 Street", 1000));
		list2.add(new House("789 Boulevard", 3000));

		List<String> listS1 = new ArrayList<>();
		listS1.add("item1");
		listS1.add("item2");
		listS1.add("item3");

		List<String> listS2 = new ArrayList<>();
		listS2.add("item1");
		listS2.add("item2");
		listS2.add("item4");

		Person person1 = new Person("John", "John2", "John3", 25, list1, listS1);
		Person person2 = new Person("John", "John", "John", 30, list2, listS2);

		// 比较差异
		DiffResult<Person> differences = DiffUtils.compareObjects(person1, person2);
		differences.getDiffs().forEach(x -> {
			System.out.println(x.getFieldName());
		});

		// 指定要更新的字段
		Set<String> fieldsToUpdate = new HashSet<>();
		fieldsToUpdate.add("name1");
		fieldsToUpdate.add("age");
		fieldsToUpdate.add("list");

		// 应用差异到指定字段
		applyDifferencesForFields(person1, person2, fieldsToUpdate);
		System.out.println("Updated target object: " + person2);
	}
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Person {
	private String name;
	private String name1;
	private String name2;
	private int age;
	private List<House> list;
	private List<String> list2;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class House {
	private String address;
	private int size;
}
