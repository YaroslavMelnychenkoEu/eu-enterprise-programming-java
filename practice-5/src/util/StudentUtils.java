package util;

import model.Student;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Утилітний клас для роботи зі студентами
 */
public class StudentUtils {
    
    /**
     * Сортує список студентів з використанням композиції компараторів
     * @param students список студентів для сортування
     * @param comparators масив компараторів для композиції
     * @return відсортований список студентів
     */
    public static List<Student> sortWithComposition(List<Student> students, Comparator<Student>... comparators) {
        if (students == null || students.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Student> result = new ArrayList<>(students);
        
        if (comparators.length == 0) {
            return result;
        }
        
        // Створюємо композитний компаратор
        Comparator<Student> compositeComparator = comparators[0];
        for (int i = 1; i < comparators.length; i++) {
            compositeComparator = compositeComparator.thenComparing(comparators[i]);
        }
        
        result.sort(compositeComparator);
        return result;
    }
    
    /**
     * Фільтрує список студентів за заданим предикатом
     * @param students список студентів для фільтрації
     * @param predicate предикат для фільтрації
     * @return відфільтрований список студентів
     */
    public static List<Student> filter(List<Student> students, java.util.function.Predicate<Student> predicate) {
        if (students == null || students.isEmpty()) {
            return new ArrayList<>();
        }
        
        return students.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }
}
