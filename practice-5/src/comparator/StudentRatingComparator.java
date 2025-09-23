package comparator;

import model.Student;
import java.util.Comparator;

/**
 * Складний компаратор для сортування студентів за рейтингом
 * Сортує за: факультетом (алфавітний порядок), середнім балом (спадання), 
 * роком навчання (зростання), ім'ям (алфавітний порядок)
 */
public class StudentRatingComparator implements Comparator<Student> {
    
    @Override
    public int compare(Student s1, Student s2) {
        // Спочатку порівнюємо за факультетом (алфавітний порядок)
        int facultyCompare = s1.getFaculty().compareTo(s2.getFaculty());
        if (facultyCompare != 0) return facultyCompare;
        
        // Далі за середнім балом (спадання)
        int gradeCompare = Double.compare(s2.getAverageGrade(), s1.getAverageGrade());
        if (gradeCompare != 0) return gradeCompare;
        
        // Потім за роком навчання (зростання)
        int yearCompare = Integer.compare(s1.getYearOfStudy(), s2.getYearOfStudy());
        if (yearCompare != 0) return yearCompare;
        
        // Нарешті за ім'ям (алфавітний порядок)
        return s1.getName().compareTo(s2.getName());
    }
}
