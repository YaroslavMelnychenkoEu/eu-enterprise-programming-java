import java.util.*;

/** ===== Абстрактна фігура ===== */
abstract class Shape implements Comparable<Shape> {
    private final String color;

    public Shape(String color) {
        if (color == null || color.isBlank()) {
            throw new IllegalArgumentException("Колір не може бути порожнім");
        }
        this.color = color.trim();
    }

    public String getColor() { return color; }

    public abstract double calculateArea();
    public abstract double calculatePerimeter();

    @Override
    public int compareTo(Shape other) {
        return Double.compare(this.calculateArea(), other.calculateArea());
    }

    @Override
    public String toString() {
        return String.format("%s (color=%s, area=%.2f, perimeter=%.2f)",
                this.getClass().getSimpleName(), color, calculateArea(), calculatePerimeter());
    }
}

/** ===== Коло ===== */
class Circle extends Shape {
    private final double radius;

    public Circle(String color, double radius) {
        super(color);
        if (radius <= 0) throw new IllegalArgumentException("Радіус має бути > 0");
        this.radius = radius;
    }

    public double getRadius() { return radius; }

    @Override
    public double calculateArea() { return Math.PI * radius * radius; }

    @Override
    public double calculatePerimeter() { return 2 * Math.PI * radius; }
}

/** ===== Прямокутник ===== */
class Rectangle extends Shape {
    private final double length;
    private final double width;

    public Rectangle(String color, double length, double width) {
        super(color);
        if (length <= 0 || width <= 0)
            throw new IllegalArgumentException("Довжина і ширина мають бути > 0");
        this.length = length;
        this.width = width;
    }

    public double getLength() { return length; }
    public double getWidth() { return width; }

    @Override
    public double calculateArea() { return length * width; }

    @Override
    public double calculatePerimeter() { return 2 * (length + width); }
}

/** ===== Трикутник ===== */
class Triangle extends Shape {
    private final double a, b, c;

    public Triangle(String color, double a, double b, double c) {
        super(color);
        if (a <= 0 || b <= 0 || c <= 0)
            throw new IllegalArgumentException("Сторони мають бути > 0");
        // Перевірка нерівностей трикутника
        if (a + b <= c || a + c <= b || b + c <= a)
            throw new IllegalArgumentException("Задані сторони не утворюють трикутник");
        this.a = a; this.b = b; this.c = c;
    }

    public double getA() { return a; }
    public double getB() { return b; }
    public double getC() { return c; }

    @Override
    public double calculateArea() {
        double p = calculatePerimeter() / 2;
        return Math.sqrt(p * (p - a) * (p - b) * (p - c)); // Формула Герона
    }

    @Override
    public double calculatePerimeter() { return a + b + c; }
}

/** ===== Демонстрація ===== */
public class ShapeDemo {
    public static void main(String[] args) {
        List<Shape> shapes = new ArrayList<>();

        shapes.add(new Circle("Red", 5));
        shapes.add(new Rectangle("Blue", 4, 6));
        shapes.add(new Triangle("Green", 3, 4, 5));

        System.out.println("--- Усі фігури ---");
        shapes.forEach(System.out::println);

        System.out.println("\n--- Відсортовані за площею ---");
        Collections.sort(shapes);
        shapes.forEach(System.out::println);

        System.out.println("\n--- Найбільша фігура ---");
        Shape max = Collections.max(shapes);
        System.out.println(max);
    }
}