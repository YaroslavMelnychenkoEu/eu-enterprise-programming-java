import java.util.*;
import java.util.logging.*;

/** ========= ВИНЯТКИ ========= */
class InvalidProductException extends RuntimeException {
    public InvalidProductException(String msg) { super(msg); }
}
class DuplicateProductException extends RuntimeException {
    public DuplicateProductException(String msg) { super(msg); }
}
class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String msg) { super(msg); }
}
class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String msg) { super(msg); }
}

/** ========= МОДЕЛІ ========= */
class Product {
    private final UUID id;
    private final String name;
    private double price;
    private int quantity;

    public Product(UUID id, String name, double price, int quantity) {
        if (id == null) throw new InvalidProductException("id продукту не може бути null");
        if (name == null || name.isBlank()) throw new InvalidProductException("Назва продукту не може бути порожньою");
        if (price < 0) throw new InvalidProductException("Ціна не може бути відʼємною");
        if (quantity < 0) throw new InvalidProductException("Кількість не може бути відʼємною");
        this.id = id;
        this.name = name.trim();
        this.price = price;
        this.quantity = quantity;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public void setPrice(double price) {
        if (price < 0) throw new InvalidProductException("Ціна не може бути відʼємною");
        this.price = price;
    }
    public void setQuantity(int quantity) {
        if (quantity < 0) throw new InvalidProductException("Кількість не може бути відʼємною");
        this.quantity = quantity;
    }

    public double totalValue() { return price * quantity; }

    @Override public String toString() {
        return String.format("Product{%s, \"%s\", price=%.2f, qty=%d}",
                id.toString().substring(0,8), name, price, quantity);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        return id.equals(((Product)o).id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}

class Category {
    private final String name;
    private final Map<UUID, Product> products = new LinkedHashMap<>();

    public Category(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Назва категорії не може бути порожньою");
        this.name = name.trim();
    }

    public String getName() { return name; }
    public Collection<Product> getProducts() { return Collections.unmodifiableCollection(products.values()); }
    public boolean containsProduct(UUID id) { return products.containsKey(id); }
    public Product getProduct(UUID id) { return products.get(id); }

    void addProduct(Product p) {
        if (p == null) throw new InvalidProductException("Product не може бути null");
        if (products.containsKey(p.getId())) throw new DuplicateProductException("Такий продукт уже є в категорії");
        products.put(p.getId(), p);
    }

    void removeProduct(UUID productId) {
        if (!products.containsKey(productId)) throw new ProductNotFoundException("Продукту немає в категорії");
        products.remove(productId);
    }

    @Override public String toString() {
        return String.format("Category{%s, items=%d}", name, products.size());
    }
}

/** ========= ІНВЕНТАР ========= */
class Inventory {
    private static final Logger log = Logger.getLogger(Inventory.class.getName());
    private final Map<String, Category> categories = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public Inventory() {
        // Налаштування простого логування в консоль
        LogManager.getLogManager().reset();
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.INFO);
        log.addHandler(ch);
        log.setLevel(Level.INFO);
        log.setUseParentHandlers(false);
    }

    /* Категорії */
    public void addCategory(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Назва категорії не може бути порожньою");
        if (categories.containsKey(name)) {
            log.info(() -> "Категорія вже існує: " + name);
            return;
        }
        categories.put(name, new Category(name));
        log.info(() -> "Додано категорію: " + name);
    }

    public void removeCategory(String name) {
        Category c = categories.remove(name);
        if (c == null) throw new CategoryNotFoundException("Категорію не знайдено: " + name);
        log.info(() -> "Видалено категорію: " + name);
    }

    public Category getCategory(String name) {
        Category c = categories.get(name);
        if (c == null) throw new CategoryNotFoundException("Категорію не знайдено: " + name);
        return c;
    }

    /* Товари */
    public void addProduct(String categoryName, Product product) {
        Category cat = getCategory(categoryName);
        // глобальна перевірка на дубль id у всьому інвентарі
        if (findProductById(product.getId()).isPresent())
            throw new DuplicateProductException("Продукт із таким id вже існує в інвентарі");
        cat.addProduct(product);
        log.info(() -> String.format("Додано %s до категорії '%s'", product, categoryName));
    }

    public void removeProduct(String categoryName, UUID productId) {
        Category cat = getCategory(categoryName);
        Product p = cat.getProduct(productId);
        cat.removeProduct(productId);
        log.info(() -> String.format("Видалено %s з категорії '%s'", p, categoryName));
    }

    public Optional<Product> findProductById(UUID id) {
        for (Category c : categories.values()) {
            if (c.containsProduct(id)) return Optional.ofNullable(c.getProduct(id));
        }
        return Optional.empty();
    }

    public List<Product> searchByName(String namePart) {
        if (namePart == null) namePart = "";
        final String needle = namePart.trim().toLowerCase();
        List<Product> res = new ArrayList<>();
        for (Category c : categories.values()) {
            for (Product p : c.getProducts()) {
                if (p.getName().toLowerCase().contains(needle)) res.add(p);
            }
        }
        log.info(() -> String.format("Пошук за назвою '%s' -> %d знайдено", needle, res.size()));
        return res;
    }

    public List<Product> searchByCategory(String categoryName) {
        Category c = getCategory(categoryName);
        List<Product> res = new ArrayList<>(c.getProducts());
        log.info(() -> String.format("Пошук у категорії '%s' -> %d знайдено", categoryName, res.size()));
        return res;
    }

    public double totalInventoryValue() {
        double sum = 0.0;
        for (Category c : categories.values()) {
            for (Product p : c.getProducts()) sum += p.totalValue();
        }
        log.info(String.format("Загальна вартість інвентарю: %.2f", sum));
        return sum;
    }

    public Collection<Category> listCategories() {
        return Collections.unmodifiableCollection(categories.values());
    }
}

/** ========= ДЕМО ========= */
public class InventoryDemo {
    public static void main(String[] args) {
        Inventory inv = new Inventory();

        // Категорії
        inv.addCategory("Electronics");
        inv.addCategory("Groceries");
        inv.addCategory("Books");

        // Товари
        Product p1 = new Product(UUID.randomUUID(), "Laptop Pro 14", 1200.0, 5);
        Product p2 = new Product(UUID.randomUUID(), "Wireless Mouse", 25.5, 30);
        Product p3 = new Product(UUID.randomUUID(), "Organic Apples", 2.1, 200);
        Product p4 = new Product(UUID.randomUUID(), "Clean Code (Book)", 38.0, 12);

        inv.addProduct("Electronics", p1);
        inv.addProduct("Electronics", p2);
        inv.addProduct("Groceries",   p3);
        inv.addProduct("Books",       p4);

        // Перелік категорій
        System.out.println("\n--- Категорії ---");
        inv.listCategories().forEach(System.out::println);

        // Пошук
        System.out.println("\n--- Пошук за назвою 'pro' ---");
        inv.searchByName("pro").forEach(System.out::println);

        System.out.println("\n--- Товари в категорії 'Electronics' ---");
        inv.searchByCategory("Electronics").forEach(System.out::println);

        // Загальна вартість
        System.out.printf("%nЗагальна вартість: %.2f%n", inv.totalInventoryValue());

        // Зміни кількостей/цін (валідація працює)
        p2.setQuantity(25);
        p4.setPrice(40.0);
        System.out.printf("Оновлена вартість: %.2f%n", inv.totalInventoryValue());

        // Видалення
        inv.removeProduct("Electronics", p2.getId());

        // Демонстрація помилок
        try {
            inv.addProduct("Unknown", new Product(UUID.randomUUID(), "X", 1, 1));
        } catch (CategoryNotFoundException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }
        try {
            inv.removeProduct("Electronics", UUID.randomUUID());
        } catch (ProductNotFoundException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }
        try {
            new Product(UUID.randomUUID(), "Bad", -10, 1);
        } catch (InvalidProductException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }

        System.out.printf("%nФінальна вартість: %.2f%n", inv.totalInventoryValue());
    }
}