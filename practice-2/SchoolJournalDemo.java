import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/* ===================== ВИНЯТКИ ===================== */
class ValidationException extends RuntimeException { public ValidationException(String m){super(m);} }
class NotFoundException extends RuntimeException { public NotFoundException(String m){super(m);} }

/* ===================== МОДЕЛІ ===================== */
class Student {
    private final UUID id;
    private final String name;

    public Student(UUID id, String name) {
        if (id == null) throw new ValidationException("Student id не може бути null");
        if (name == null || name.isBlank()) throw new ValidationException("Ім'я студента не може бути порожнім");
        this.id = id; this.name = name.trim();
    }
    public UUID getId(){ return id; }
    public String getName(){ return name; }
    @Override public String toString(){ return String.format("Student{%s, %s}", id.toString().substring(0,8), name); }
}

class Teacher {
    private final UUID id;
    private final String name;
    private final List<Subject> subjects = new ArrayList<>();
    public Teacher(UUID id, String name){
        if (id==null) throw new ValidationException("Teacher id не може бути null");
        if (name==null || name.isBlank()) throw new ValidationException("Ім'я викладача не може бути порожнім");
        this.id=id; this.name=name.trim();
    }
    public UUID getId(){ return id; }
    public String getName(){ return name; }
    List<Subject> _subjects(){ return subjects; } // внутрішній доступ
    @Override public String toString(){ return String.format("Teacher{%s, %s}", id.toString().substring(0,8), name); }
}

class Grade {
    private final UUID id;
    private final Student student;
    private final int value;       // 1..12
    private final LocalDate date;

    public Grade(Student student, int value, LocalDate date) {
        if (student == null) throw new ValidationException("Student не може бути null");
        if (date == null) throw new ValidationException("Дата не може бути null");
        if (value < 1 || value > 12) throw new ValidationException("Оцінка має бути в діапазоні 1..12");
        this.id = UUID.randomUUID();
        this.student = student; this.value = value; this.date = date;
    }
    public UUID getId(){ return id; }
    public Student getStudent(){ return student; }
    public int getValue(){ return value; }
    public LocalDate getDate(){ return date; }
    @Override public String toString(){ return String.format("Grade{%s, %s, %d, %s}",
            id.toString().substring(0,8), student.getName(), value, date); }
}

enum AttendanceStatus { PRESENT, ABSENT, LATE }
class AttendanceRecord {
    private final Student student;
    private final LocalDate date;
    private final AttendanceStatus status;
    public AttendanceRecord(Student student, LocalDate date, AttendanceStatus status){
        if (student==null) throw new ValidationException("Student не може бути null");
        if (date==null) throw new ValidationException("Дата не може бути null");
        if (status==null) throw new ValidationException("Статус не може бути null");
        this.student=student; this.date=date; this.status=status;
    }
    public Student getStudent(){ return student; }
    public LocalDate getDate(){ return date; }
    public AttendanceStatus getStatus(){ return status; }
    @Override public String toString(){ return String.format("Attendance{%s, %s, %s}", student.getName(), date, status); }
}

class Subject {
    private final UUID id;
    private final String name;
    private final Teacher teacher;
    private final List<Student> students = new ArrayList<>();
    private final List<Grade> grades = new ArrayList<>();
    private final List<AttendanceRecord> attendance = new ArrayList<>();

    public Subject(String name, Teacher teacher, List<Student> initialStudents) {
        if (name==null || name.isBlank()) throw new ValidationException("Назва предмету не може бути порожньою");
        if (teacher==null) throw new ValidationException("Викладач не може бути null");
        this.id = UUID.randomUUID();
        this.name = name.trim();
        this.teacher = teacher;
        if (initialStudents != null) this.students.addAll(initialStudents);
        teacher._subjects().add(this);
    }

    public UUID getId(){ return id; }
    public String getName(){ return name; }
    public Teacher getTeacher(){ return teacher; }
    public List<Student> getStudents(){ return Collections.unmodifiableList(students); }
    public List<Grade> getGrades(){ return Collections.unmodifiableList(grades); }
    public List<AttendanceRecord> getAttendance(){ return Collections.unmodifiableList(attendance); }

    public void addStudent(Student s){
        if (s==null) throw new ValidationException("Student не може бути null");
        if (students.stream().anyMatch(x -> x.getId().equals(s.getId())))
            throw new ValidationException("Студент уже доданий до предмету");
        students.add(s);
    }

    public void removeStudent(Student s){
        students.removeIf(x -> x.getId().equals(s.getId()));
    }

    /* --- Виставлення оцінок --- */
    public Grade setGrade(Student s, int value, LocalDate date){
        assertStudentEnrolled(s);
        Grade g = new Grade(s, value, date);
        grades.add(g);
        return g;
    }

    /* --- Відвідуваність --- */
    public AttendanceRecord markAttendance(Student s, LocalDate date, AttendanceStatus status){
        assertStudentEnrolled(s);
        AttendanceRecord rec = new AttendanceRecord(s, date, status);
        attendance.add(rec);
        return rec;
    }

    /* --- Середні бали --- */
    public double averageForStudent(Student s){
        assertStudentEnrolled(s);
        return grades.stream().filter(g -> g.getStudent().getId().equals(s.getId()))
                .mapToInt(Grade::getValue).average().orElse(Double.NaN);
    }
    public double averageForSubject(){
        return grades.stream().mapToInt(Grade::getValue).average().orElse(Double.NaN);
    }

    /* --- Звіти --- */
    public String reportForStudent(Student s){
        assertStudentEnrolled(s);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Звіт по предмету '%s' для %s%n", name, s.getName()));
        List<Grade> gs = grades.stream().filter(g -> g.getStudent().getId().equals(s.getId()))
                .sorted(Comparator.comparing(Grade::getDate)).collect(Collectors.toList());
        if (gs.isEmpty()) sb.append("  Оцінок немає\n");
        else {
            for (Grade g: gs) sb.append(String.format("  %s: %d%n", g.getDate(), g.getValue()));
            sb.append(String.format("  Середній бал: %.2f%n", averageForStudent(s)));
        }
        long present = attendance.stream().filter(a -> a.getStudent().getId().equals(s.getId()) && a.getStatus()==AttendanceStatus.PRESENT).count();
        long total = attendance.stream().filter(a -> a.getStudent().getId().equals(s.getId())).count();
        double rate = (total==0)? Double.NaN : present * 100.0 / total;
        sb.append(String.format("  Відвідуваність: %s%n", Double.isNaN(rate) ? "н/д" : String.format("%.1f%%", rate)));
        return sb.toString();
    }

    public String reportForSubject(){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Звіт по предмету '%s' (викладач: %s)%n", name, teacher.getName()));
        sb.append("Cтудентів: ").append(students.size()).append(", оцінок: ").append(grades.size()).append("\n");
        sb.append(String.format("Середній бал по предмету: %s%n",
                grades.isEmpty() ? "н/д" : String.format("%.2f", averageForSubject())));

        // Розподіл оцінок
        Map<Integer, Long> distribution = grades.stream()
                .collect(Collectors.groupingBy(Grade::getValue, TreeMap::new, Collectors.counting()));
        sb.append("Розподіл оцінок: ").append(distribution).append("\n");

        // Мін/макс
        grades.stream().mapToInt(Grade::getValue).max().ifPresent(max -> sb.append("Макс оцінка: ").append(max).append("\n"));
        grades.stream().mapToInt(Grade::getValue).min().ifPresent(min -> sb.append("Мін оцінка: ").append(min).append("\n"));

        // Загальна відвідуваність
        long total = attendance.size();
        long present = attendance.stream().filter(a -> a.getStatus()==AttendanceStatus.PRESENT).count();
        sb.append(String.format("Відвідуваність загальна: %s%n",
                total==0 ? "н/д" : String.format("%.1f%%", present*100.0/total)));

        return sb.toString();
    }

    private void assertStudentEnrolled(Student s){
        if (s==null) throw new ValidationException("Student не може бути null");
        boolean enrolled = students.stream().anyMatch(x -> x.getId().equals(s.getId()));
        if (!enrolled) throw new NotFoundException("Студента не знайдено серед слухачів предмету");
    }

    @Override public String toString(){
        return String.format("Subject{%s, %s, teacher=%s, students=%d}",
                id.toString().substring(0,8), name, teacher.getName(), students.size());
    }
}

/* ===================== ЖУРНАЛ (ФАСАД) ===================== */
class Journal {
    private final Map<UUID, Student> students = new LinkedHashMap<>();
    private final Map<UUID, Teacher> teachers = new LinkedHashMap<>();
    private final Map<UUID, Subject> subjects = new LinkedHashMap<>();

    public Student addStudent(String name){
        Student s = new Student(UUID.randomUUID(), name);
        students.put(s.getId(), s); return s;
    }
    public Teacher addTeacher(String name){
        Teacher t = new Teacher(UUID.randomUUID(), name);
        teachers.put(t.getId(), t); return t;
    }
    public Subject addSubject(String name, Teacher teacher, List<Student> roster){
        Subject sub = new Subject(name, teacher, roster);
        subjects.put(sub.getId(), sub); return sub;
    }

    public Student getStudent(UUID id){
        Student s = students.get(id);
        if (s==null) throw new NotFoundException("Студента не знайдено");
        return s;
    }
    public Subject getSubject(UUID id){
        Subject s = subjects.get(id);
        if (s==null) throw new NotFoundException("Предмет не знайдено");
        return s;
    }

    /* Операції в один рядок */
    public Grade putGrade(Subject subject, Student student, int value, LocalDate date){
        return subject.setGrade(student, value, date);
    }
    public AttendanceRecord mark(Subject subject, Student student, LocalDate date, AttendanceStatus st){
        return subject.markAttendance(student, date, st);
    }

    public Collection<Student> listStudents(){ return Collections.unmodifiableCollection(students.values()); }
    public Collection<Teacher> listTeachers(){ return Collections.unmodifiableCollection(teachers.values()); }
    public Collection<Subject> listSubjects(){ return Collections.unmodifiableCollection(subjects.values()); }
}

/* ===================== ДЕМО ===================== */
public class SchoolJournalDemo {
    public static void main(String[] args) {
        Journal j = new Journal();

        // Вчителі
        Teacher tMath = j.addTeacher("Олена Іванівна");
        Teacher tCS   = j.addTeacher("Віктор Петрович");

        // Студенти
        Student s1 = j.addStudent("Андрій");
        Student s2 = j.addStudent("Марія");
        Student s3 = j.addStudent("Олег");

        // Предмети
        Subject math = j.addSubject("Математика", tMath, Arrays.asList(s1, s2, s3));
        Subject cs   = j.addSubject("Програмування", tCS, Arrays.asList(s1, s2));

        // Оцінки (валідація 1..12)
        j.putGrade(math, s1, 10, LocalDate.now().minusDays(5));
        j.putGrade(math, s2, 8, LocalDate.now().minusDays(4));
        j.putGrade(math, s3, 12, LocalDate.now().minusDays(3));
        j.putGrade(math, s1, 9, LocalDate.now().minusDays(1));

        j.putGrade(cs, s1, 11, LocalDate.now().minusDays(2));
        j.putGrade(cs, s2, 7, LocalDate.now().minusDays(1));

        // Відвідуваність
        j.mark(math, s1, LocalDate.now().minusDays(5), AttendanceStatus.PRESENT);
        j.mark(math, s2, LocalDate.now().minusDays(5), AttendanceStatus.ABSENT);
        j.mark(math, s3, LocalDate.now().minusDays(5), AttendanceStatus.LATE);

        j.mark(math, s1, LocalDate.now().minusDays(1), AttendanceStatus.PRESENT);
        j.mark(math, s2, LocalDate.now().minusDays(1), AttendanceStatus.PRESENT);
        j.mark(math, s3, LocalDate.now().minusDays(1), AttendanceStatus.PRESENT);

        j.mark(cs, s1, LocalDate.now().minusDays(2), AttendanceStatus.PRESENT);
        j.mark(cs, s2, LocalDate.now().minusDays(2), AttendanceStatus.ABSENT);

        // Звіти по студенту та предмету
        System.out.println("--- Звіт: Математика / Андрій ---");
        System.out.println(math.reportForStudent(s1));

        System.out.println("--- Звіт: Програмування / Марія ---");
        System.out.println(cs.reportForStudent(s2));

        System.out.println("--- Загальний звіт по Математиці ---");
        System.out.println(math.reportForSubject());

        System.out.println("--- Загальний звіт по Програмуванню ---");
        System.out.println(cs.reportForSubject());

        // Демонстрація помилки валідації
        try {
            j.putGrade(math, s1, 15, LocalDate.now());
        } catch (ValidationException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }

        // Демонстрація: спроба поставити оцінку не записаному студенту
        try {
            Student stranger = new Student(UUID.randomUUID(), "Гість");
            j.putGrade(math, stranger, 7, LocalDate.now());
        } catch (NotFoundException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }
    }
}