package comparator;

import model.Student;
import java.util.Comparator;

/**
 * Компаратор для сортування студентів за ім'ям (алфавітний порядок)
 */
public class NameComparator implements Comparator<Student> {
    
    @Override
    public int compare(Student s1, Student s2) {
        return s1.getName().compareTo(s2.getName());
    }
}
