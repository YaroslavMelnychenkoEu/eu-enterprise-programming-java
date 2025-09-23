package comparator;

import model.Student;
import java.util.Comparator;

/**
 * Компаратор для сортування студентів за роком навчання (зростання)
 */
public class YearComparator implements Comparator<Student> {
    
    @Override
    public int compare(Student s1, Student s2) {
        return Integer.compare(s1.getYearOfStudy(), s2.getYearOfStudy());
    }
}
