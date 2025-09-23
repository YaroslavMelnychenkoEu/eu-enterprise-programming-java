package test;

import model.Student;
import comparator.*;
import util.StudentUtils;
import util.StudentFilter;
import java.util.*;

/**
 * Простий тестовий клас для перевірки функціональності
 */
public class StudentTest {
    
    public static void main(String[] args) {
        System.out.println("=== Тестування системи сортування та фільтрації ===\n");
        
        // Створення тестових даних
        List<Student> students = Arrays.asList(
            new Student("Анна", 20, 4.5, "CS", 2),
            new Student("Борис", 19, 4.2, "Math", 1),
            new Student("Віктор", 21, 4.8, "CS", 3),
            new Student("Галина", 20, 4.6, "Physics", 2)
        );
        
        System.out.println("Початковий список:");
        students.forEach(System.out::println);
        System.out.println();
        
        // Тест сортування за ім'ям
        System.out.println("Сортування за ім'ям:");
        List<Student> sortedByName = new ArrayList<>(students);
        sortedByName.sort(new NameComparator());
        sortedByName.forEach(System.out::println);
        System.out.println();
        
        // Тест фільтрації
        System.out.println("Студенти з середнім балом >= 4.5:");
        List<Student> highGradeStudents = StudentUtils.filter(students, StudentFilter.highGradeStudents(4.5));
        highGradeStudents.forEach(System.out::println);
        System.out.println();
        
        // Тест композиції компараторів
        System.out.println("Композиція: факультет + середній бал:");
        List<Student> compositeSorted = StudentUtils.sortWithComposition(
            students,
            new NameComparator(),
            new GradeComparator()
        );
        compositeSorted.forEach(System.out::println);
        
        System.out.println("\n=== Всі тести пройшли успішно! ===");
    }
}
