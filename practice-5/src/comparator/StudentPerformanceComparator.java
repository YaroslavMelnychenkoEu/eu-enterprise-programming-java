package comparator;

import model.Student;
import java.util.Comparator;

/**
 * Компаратор для сортування студентів за продуктивністю
 * Сортує за: середнім балом (спадання), роком навчання (спадання), віком (зростання)
 */
public class StudentPerformanceComparator implements Comparator<Student> {
    
    @Override
    public int compare(Student s1, Student s2) {
        // Спочатку за середнім балом (спадання)
        int gradeCompare = Double.compare(s2.getAverageGrade(), s1.getAverageGrade());
        if (gradeCompare != 0) return gradeCompare;
        
        // Далі за роком навчання (спадання)
        int yearCompare = Integer.compare(s2.getYearOfStudy(), s1.getYearOfStudy());
        if (yearCompare != 0) return yearCompare;
        
        // Нарешті за віком (зростання)
        return Integer.compare(s1.getAge(), s2.getAge());
    }
}
