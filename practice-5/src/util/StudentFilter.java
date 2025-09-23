package util;

import model.Student;
import java.util.function.Predicate;

/**
 * Клас з предикатами для фільтрації студентів
 */
public class StudentFilter {
    
    /**
     * Фільтрація за факультетом
     * @param faculty назва факультету
     * @return предикат для фільтрації
     */
    public static Predicate<Student> byFaculty(String faculty) {
        return student -> faculty.equals(student.getFaculty());
    }
    
    /**
     * Фільтрація за діапазоном середнього балу
     * @param min мінімальний бал
     * @param max максимальний бал
     * @return предикат для фільтрації
     */
    public static Predicate<Student> byGradeRange(double min, double max) {
        return student -> student.getAverageGrade() >= min && student.getAverageGrade() <= max;
    }
    
    /**
     * Фільтрація за роком навчання
     * @param year рік навчання
     * @return предикат для фільтрації
     */
    public static Predicate<Student> byYearOfStudy(int year) {
        return student -> student.getYearOfStudy() == year;
    }
    
    /**
     * Фільтрація за віковим діапазоном
     * @param minAge мінімальний вік
     * @param maxAge максимальний вік
     * @return предикат для фільтрації
     */
    public static Predicate<Student> byAgeRange(int minAge, int maxAge) {
        return student -> student.getAge() >= minAge && student.getAge() <= maxAge;
    }
    
    /**
     * Фільтрація відмінників (середній бал > 4.5)
     * @return предикат для фільтрації відмінників
     */
    public static Predicate<Student> excellentStudents() {
        return student -> student.getAverageGrade() > 4.5;
    }
    
    /**
     * Фільтрація студентів з високим середнім балом
     * @param minGrade мінімальний бал
     * @return предикат для фільтрації
     */
    public static Predicate<Student> highGradeStudents(double minGrade) {
        return student -> student.getAverageGrade() >= minGrade;
    }
}
