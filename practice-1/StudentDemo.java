import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.UUID;

/** Власний виняток для валідації студентів */
class StudentValidationException extends RuntimeException {
    public StudentValidationException(String message) {
        super(message);
    }
}

/** Модель студента */
class Student {
    private final UUID id;          // унікальний ідентифікатор
    private final String firstName; // ім'я
    private final String lastName;  // прізвище
    private final LocalDate birthDate; // дата народження
    private final double averageGrade; // середній бал (0..5)

    public Student(UUID id, String firstName, String lastName, LocalDate birthDate, double averageGrade) {
        // Базові перевірки на null/порожні значення
        if (id == null) {
            throw new StudentValidationException("id не може бути null");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new StudentValidationException("Ім'я не може бути порожнім");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new StudentValidationException("Прізвище не може бути порожнім");
        }
        if (birthDate == null) {
            throw new StudentValidationException("Дата народження не може бути null");
        }

        // Перевірка дати народження: не в майбутньому і не старше 120 років
        LocalDate today = LocalDate.now();
        if (birthDate.isAfter(today)) {
            throw new StudentValidationException("Дата народження не може бути в майбутньому");
        }
        if (birthDate.isBefore(today.minusYears(120))) {
            throw new StudentValidationException("Дата народження нереалістична (старше 120 років)");
        }

        // Перевірка середнього балу (припустимо шкала 0..5 включно)
        if (averageGrade < 0.0 || averageGrade > 5.0) {
            throw new StudentValidationException("Середній бал має бути в діапазоні 0.0..5.0");
        }

        this.id = id;
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.birthDate = birthDate;
        this.averageGrade = averageGrade;
    }

    /** Обчислити вік у роках і місяцях відносно сьогодні */
    public Period getAge() {
        return Period.between(birthDate, LocalDate.now());
    }

    /** Повертає масив [років, місяців] для зручності */
    public int[] getAgeYearsMonths() {
        Period p = getAge();
        return new int[]{p.getYears(), p.getMonths()};
    }

    /** Чи є студент відмінником (середній бал >= 4.5) */
    public boolean isExcellent() {
        return averageGrade >= 4.5;
    }

    /** Зручне текстове представлення */
    @Override
    public String toString() {
        int[] ym = getAgeYearsMonths();
        return String.format(
                "Студент #%s%nПІБ: %s %s%nДата народження: %s%nВік: %d років %d місяців%nСередній бал: %.2f%nВідмінник: %s",
                id, firstName, lastName, birthDate, ym[0], ym[1], averageGrade, isExcellent() ? "так" : "ні"
        );
    }

    // Геттери (за потреби)
    public UUID getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getBirthDate() { return birthDate; }
    public double getAverageGrade() { return averageGrade; }

    // equals/hashCode (опційно, але корисно)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        Student student = (Student) o;
        return id.equals(student.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

/** Тестовий клас-демонстрація */
public class StudentDemo {
    public static void main(String[] args) {
        // Коректний приклад
        Student s1 = new Student(
                UUID.randomUUID(),
                "Іван",
                "Петренко",
                LocalDate.of(2003, 5, 12),
                4.7
        );

        System.out.println("=== Коректний студент ===");
        System.out.println(s1);
        System.out.println();

        // Перевірка окремих методів
        int[] age = s1.getAgeYearsMonths();
        System.out.printf("Вік (окремо): %d років, %d місяців%n", age[0], age[1]);
        System.out.printf("Чи відмінник? %s%n", s1.isExcellent() ? "так" : "ні");
        System.out.println();

        // Невдалий приклад (помилка валідації середнього балу)
        try {
            Student bad = new Student(
                    UUID.randomUUID(),
                    "Марія",
                    "Коваленко",
                    LocalDate.of(2010, 2, 28),
                    6.2 // за межами діапазону 0..5
            );
            System.out.println(bad); // не дійде сюди
        } catch (StudentValidationException ex) {
            System.out.println("=== Очікувана помилка валідації ===");
            System.out.println(ex.getMessage());
        }

        // Невдалий приклад (майбутня дата народження)
        try {
            Student bad2 = new Student(
                    UUID.randomUUID(),
                    "Олег",
                    "Сидоренко",
                    LocalDate.now().plusDays(1), // у майбутньому
                    3.9
            );
            System.out.println(bad2);
        } catch (StudentValidationException ex) {
            System.out.println("=== Очікувана помилка валідації ===");
            System.out.println(ex.getMessage());
        }
    }
}