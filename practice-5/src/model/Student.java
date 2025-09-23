package model;

/**
 * Клас Student представляє студента з основними характеристиками
 */
public class Student {
    private String name;
    private int age;
    private double averageGrade;
    private String faculty;
    private int yearOfStudy;

    /**
     * Конструктор з усіма параметрами
     * @param name ім'я студента
     * @param age вік студента
     * @param averageGrade середній бал
     * @param faculty факультет
     * @param yearOfStudy рік навчання
     */
    public Student(String name, int age, double averageGrade, String faculty, int yearOfStudy) {
        this.name = name;
        this.age = age;
        this.averageGrade = averageGrade;
        this.faculty = faculty;
        this.yearOfStudy = yearOfStudy;
    }

    // Геттери для всіх полів
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public double getAverageGrade() {
        return averageGrade;
    }

    public String getFaculty() {
        return faculty;
    }

    public int getYearOfStudy() {
        return yearOfStudy;
    }

    /**
     * Метод toString() для представлення об'єкта у вигляді рядка
     */
    @Override
    public String toString() {
        return String.format("Student{name='%s', age=%d, averageGrade=%.2f, faculty='%s', yearOfStudy=%d}",
                name, age, averageGrade, faculty, yearOfStudy);
    }

    /**
     * Метод equals() для порівняння об'єктів
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Student student = (Student) obj;
        return age == student.age &&
               Double.compare(student.averageGrade, averageGrade) == 0 &&
               yearOfStudy == student.yearOfStudy &&
               name.equals(student.name) &&
               faculty.equals(student.faculty);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + age;
        result = 31 * result + Double.hashCode(averageGrade);
        result = 31 * result + faculty.hashCode();
        result = 31 * result + yearOfStudy;
        return result;
    }
}
