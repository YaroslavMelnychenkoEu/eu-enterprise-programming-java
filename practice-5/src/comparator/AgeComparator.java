package comparator;

import model.Student;
import java.util.Comparator;

/**
 * Компаратор для сортування студентів за віком (зростання)
 */
public class AgeComparator implements Comparator<Student> {
    
    @Override
    public int compare(Student s1, Student s2) {
        return Integer.compare(s1.getAge(), s2.getAge());
    }
}
