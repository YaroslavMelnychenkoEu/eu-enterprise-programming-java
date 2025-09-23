# Практична робота №5: Розробка системи сортування та фільтрації з Comparator

## Опис проекту

Цей проект демонструє використання інтерфейсу `Comparator` в Java для створення гнучкої системи сортування та фільтрації даних студентів.

## Структура проекту

```
src/
├── model/
│   └── Student.java                    # Модель студента
├── comparator/
│   ├── NameComparator.java             # Сортування за ім'ям
│   ├── AgeComparator.java              # Сортування за віком
│   ├── GradeComparator.java            # Сортування за середнім балом
│   ├── YearComparator.java             # Сортування за роком навчання
│   ├── StudentRatingComparator.java    # Складний компаратор за рейтингом
│   └── StudentPerformanceComparator.java # Складний компаратор за продуктивністю
├── util/
│   ├── StudentUtils.java               # Утилітні методи для роботи зі студентами
│   └── StudentFilter.java              # Предикати для фільтрації
├── demo/
│   └── DemoApplication.java            # Демонстраційна програма
└── test/
    └── StudentTest.java                # Тестовий клас
```

## Функціональність

### 1. Клас Student
- Поля: `name`, `age`, `averageGrade`, `faculty`, `yearOfStudy`
- Конструктор з усіма параметрами
- Геттери для всіх полів
- Методи `toString()`, `equals()`, `hashCode()`

### 2. Прості компаратори
- **NameComparator**: сортування за ім'ям (алфавітний порядок)
- **AgeComparator**: сортування за віком (зростання)
- **GradeComparator**: сортування за середнім балом (спадання)
- **YearComparator**: сортування за роком навчання (зростання)

### 3. Складні компаратори
- **StudentRatingComparator**: сортування за факультетом → середнім балом → роком навчання → ім'ям
- **StudentPerformanceComparator**: сортування за середнім балом → роком навчання → віком

### 4. Утилітні класи
- **StudentUtils**: методи для композиції компараторів та фільтрації
- **StudentFilter**: предикати для різних типів фільтрації

### 5. Демонстраційні можливості
- Сортування за рейтингом
- Пошук відмінників певного факультету
- Сортування студентів певного року за віком
- Топ-5 студентів за середнім балом
- Власні ланцюжки фільтрації та сортування

## Компіляція та запуск

### Компіляція
```bash
javac -d . src/model/*.java src/comparator/*.java src/util/*.java src/demo/*.java src/test/*.java
```

### Запуск демонстрації
```bash
java demo.DemoApplication
```

### Запуск тестів
```bash
java test.StudentTest
```

## Приклади використання

### Композиція компараторів
```java
List<Student> sorted = StudentUtils.sortWithComposition(
    students,
    new NameComparator(),
    new GradeComparator(),
    new AgeComparator()
);
```

### Фільтрація з предикатами
```java
List<Student> excellentStudents = StudentUtils.filter(
    students,
    StudentFilter.excellentStudents()
);
```

### Складне сортування
```java
students.sort(new StudentRatingComparator());
```

## Результати тестування

Проект успішно компілюється та виконується. Всі функції працюють коректно:
- ✅ Сортування за різними критеріями
- ✅ Фільтрація за різними умовами
- ✅ Композиція компараторів
- ✅ Складні алгоритми сортування
- ✅ Демонстраційні сценарії

## Технічні особливості

- Використання Java 8+ функціональних інтерфейсів
- Потокова обробка даних (Stream API)
- Композиція компараторів через `thenComparing()`
- Лямбда-вирази для предикатів
- Правильна реалізація `equals()` та `hashCode()`
