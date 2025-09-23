package demo;

import model.Student;
import comparator.*;
import util.StudentUtils;
import util.StudentFilter;
import java.util.*;

/**
 * Демонстраційний клас для показу роботи системи сортування та фільтрації
 */
public class DemoApplication {
    
    public static void main(String[] args) {
        System.out.println("=== Демонстрація системи сортування та фільтрації з Comparator ===\n");
        
        // Створення списку з мінімум 10 студентів різних факультетів
        List<Student> students = createStudentList();
        
        System.out.println("Початковий список студентів:");
        printStudents(students);
        System.out.println();
        
        // Виконання операцій згідно з завданням
        
        // 1. Відсортувати всіх студентів за рейтингом (використовуючи StudentRatingComparator)
        System.out.println("1. Сортування за рейтингом (StudentRatingComparator):");
        List<Student> sortedByRating = new ArrayList<>(students);
        sortedByRating.sort(new StudentRatingComparator());
        printStudents(sortedByRating);
        System.out.println();
        
        // 2. Знайти всіх відмінників (середній бал > 4.5) певного факультету
        System.out.println("2. Відмінники факультету 'Computer Science' (середній бал > 4.5):");
        List<Student> excellentCSStudents = StudentUtils.filter(
            StudentUtils.filter(students, StudentFilter.byFaculty("Computer Science")),
            StudentFilter.excellentStudents()
        );
        printStudents(excellentCSStudents);
        System.out.println();
        
        // 3. Відсортувати студентів певного року навчання за віком
        System.out.println("3. Студенти 2-го року навчання, відсортовані за віком:");
        List<Student> secondYearStudents = StudentUtils.filter(students, StudentFilter.byYearOfStudy(2));
        secondYearStudents.sort(new AgeComparator());
        printStudents(secondYearStudents);
        System.out.println();
        
        // 4. Знайти топ-5 студентів за середнім балом серед усіх факультетів
        System.out.println("4. Топ-5 студентів за середнім балом:");
        List<Student> topStudents = new ArrayList<>(students);
        topStudents.sort(new GradeComparator());
        List<Student> top5 = topStudents.subList(0, Math.min(5, topStudents.size()));
        printStudents(top5);
        System.out.println();
        
        // 5. Створити власний ланцюжок фільтрації та сортування
        System.out.println("5. Власний ланцюжок: студенти 3-го року з балом >= 4.0, відсортовані за віком:");
        List<Student> customChain = StudentUtils.filter(
            StudentUtils.filter(students, StudentFilter.byYearOfStudy(3)),
            StudentFilter.highGradeStudents(4.0)
        );
        customChain.sort(new AgeComparator());
        printStudents(customChain);
        System.out.println();
        
        // Додаткові демонстрації композиції компараторів
        System.out.println("6. Демонстрація композиції компараторів (факультет + середній бал + вік):");
        List<Student> compositeSorted = StudentUtils.sortWithComposition(
            students,
            new NameComparator(), // за ім'ям
            new GradeComparator(), // потім за середнім балом
            new AgeComparator()    // нарешті за віком
        );
        printStudents(compositeSorted);
        System.out.println();
        
        // Демонстрація StudentPerformanceComparator
        System.out.println("7. Сортування за продуктивністю (StudentPerformanceComparator):");
        List<Student> performanceSorted = new ArrayList<>(students);
        performanceSorted.sort(new StudentPerformanceComparator());
        printStudents(performanceSorted);
    }
    
    /**
     * Створює список студентів для демонстрації
     */
    private static List<Student> createStudentList() {
        List<Student> students = new ArrayList<>();
        
        // Студенти факультету Computer Science
        students.add(new Student("Олексій Петренко", 20, 4.8, "Computer Science", 2));
        students.add(new Student("Марія Коваленко", 19, 4.6, "Computer Science", 1));
        students.add(new Student("Дмитро Іваненко", 21, 4.2, "Computer Science", 3));
        students.add(new Student("Анна Сидоренко", 20, 4.9, "Computer Science", 2));
        students.add(new Student("Ігор Мельник", 22, 3.8, "Computer Science", 4));
        
        // Студенти факультету Mathematics
        students.add(new Student("Катерина Бондаренко", 19, 4.7, "Mathematics", 1));
        students.add(new Student("Володимир Шевченко", 21, 4.3, "Mathematics", 3));
        students.add(new Student("Олена Ткаченко", 20, 4.5, "Mathematics", 2));
        students.add(new Student("Сергій Морозенко", 22, 4.1, "Mathematics", 4));
        
        // Студенти факультету Physics
        students.add(new Student("Наталія Романенко", 20, 4.4, "Physics", 2));
        students.add(new Student("Андрій Гриценко", 21, 4.0, "Physics", 3));
        students.add(new Student("Вікторія Лисенко", 19, 4.6, "Physics", 1));
        students.add(new Student("Максим Панченко", 23, 3.9, "Physics", 4));
        
        // Студенти факультету Economics
        students.add(new Student("Юлія Федоренко", 20, 4.3, "Economics", 2));
        students.add(new Student("Роман Кравченко", 21, 4.1, "Economics", 3));
        
        return students;
    }
    
    /**
     * Виводить список студентів на консоль
     */
    private static void printStudents(List<Student> students) {
        if (students.isEmpty()) {
            System.out.println("Список порожній");
            return;
        }
        
        for (int i = 0; i < students.size(); i++) {
            System.out.printf("%2d. %s%n", i + 1, students.get(i));
        }
    }
}
