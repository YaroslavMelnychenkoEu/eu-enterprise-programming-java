package comparator;

import model.Student;
import java.util.Comparator;

/**
 * Компаратор для сортування студентів за середнім балом (спадання)
 */
public class GradeComparator implements Comparator<Student> {
    
    @Override
    public int compare(Student s1, Student s2) {
        return Double.compare(s2.getAverageGrade(), s1.getAverageGrade());
    }
}
