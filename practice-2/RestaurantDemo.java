import java.util.*;

/* ===================== ПЕРЕЛІКИ ===================== */
enum Category { STARTER, MAIN, DESSERT, DRINK }
enum OrderStatus { NEW, IN_PROGRESS, READY, COMPLETED, CANCELED }

/* ===================== ВИНЯТКИ ===================== */
class ItemUnavailableException extends RuntimeException {
    public ItemUnavailableException(String msg) { super(msg); }
}
class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String msg) { super(msg); }
}
class OrderNotEditableException extends RuntimeException {
    public OrderNotEditableException(String msg) { super(msg); }
}
class InvalidOperationException extends RuntimeException {
    public InvalidOperationException(String msg) { super(msg); }
}

/* ===================== МОДЕЛІ ===================== */
class MenuItem {
    private final UUID id;
    private final String name;
    private final double price;
    private final Category category;
    private boolean available;

    public MenuItem(UUID id, String name, double price, Category category, boolean available) {
        if (id == null) throw new IllegalArgumentException("id не може бути null");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Назва не може бути порожньою");
        if (price < 0) throw new IllegalArgumentException("Ціна не може бути від’ємною");
        if (category == null) throw new IllegalArgumentException("Категорія не може бути null");
        this.id = id;
        this.name = name.trim();
        this.price = price;
        this.category = category;
        this.available = available;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public Category getCategory() { return category; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override public String toString() {
        return String.format("MenuItem{%s, \"%s\", %.2f, %s, %s}",
                id.toString().substring(0,8), name, price, category, available ? "available" : "off");
    }
}

class Customer {
    private final UUID id;
    private final String name;
    private int completedOrders; // для визначення постійного клієнта

    public Customer(UUID id, String name) {
        if (id == null) throw new IllegalArgumentException("id не може бути null");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Ім’я не може бути порожнім");
        this.id = id;
        this.name = name.trim();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }

    public int getCompletedOrders() { return completedOrders; }
    public void incCompletedOrders() { completedOrders++; }

    /** Постійний клієнт після N успішних замовлень */
    public boolean isLoyal() { return completedOrders >= 3; }
}

class OrderItem {
    private final MenuItem item;
    private int quantity;

    public OrderItem(MenuItem item, int quantity) {
        if (item == null) throw new IllegalArgumentException("item не може бути null");
        if (quantity <= 0) throw new IllegalArgumentException("quantity має бути > 0");
        this.item = item;
        this.quantity = quantity;
    }

    public MenuItem getItem() { return item; }
    public int getQuantity() { return quantity; }
    public void add(int q) {
        if (q <= 0) throw new IllegalArgumentException("q має бути > 0");
        quantity += q;
    }
    public void remove(int q) {
        if (q <= 0) throw new IllegalArgumentException("q має бути > 0");
        if (q > quantity) throw new IllegalArgumentException("Не можна зняти більше, ніж у позиції");
        quantity -= q;
    }
    public double lineTotal() { return item.getPrice() * quantity; }

    @Override public String toString() {
        return String.format("%s x%d (%.2f)", item.getName(), quantity, lineTotal());
    }
}

class Order {
    private final UUID id;
    private final UUID customerId;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.NEW;
    private double totalPrice = 0.0; // кешоване значення (перерахунок при змінах)

    public Order(UUID id, UUID customerId) {
        if (id == null || customerId == null) throw new IllegalArgumentException("id/customerId не можуть бути null");
        this.id = id;
        this.customerId = customerId;
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public double getTotalPrice() { return totalPrice; }

    public void ensureEditable() {
        if (status == OrderStatus.CANCELED || status == OrderStatus.COMPLETED)
            throw new OrderNotEditableException("Замовлення у статусі " + status + " — редагування заборонене");
    }

    public void addItem(MenuItem item, int qty) {
        ensureEditable();
        if (!item.isAvailable()) throw new ItemUnavailableException("Страва недоступна: " + item.getName());
        Optional<OrderItem> existing = items.stream().filter(oi -> oi.getItem().getId().equals(item.getId())).findFirst();
        if (existing.isPresent()) {
            existing.get().add(qty);
        } else {
            items.add(new OrderItem(item, qty));
        }
        recalcTotal(null); // без знижки на цьому етапі
    }

    public void removeItem(UUID menuItemId, int qty) {
        ensureEditable();
        OrderItem oi = items.stream().filter(x -> x.getItem().getId().equals(menuItemId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Позицію не знайдено в замовленні"));
        oi.remove(qty);
        if (oi.getQuantity() == 0) items.remove(oi);
        recalcTotal(null);
    }

    public void applyDiscountIfEligible(Customer c) {
        // Базова логіка: 10% знижки для постійних клієнтів
        if (c != null && c.isLoyal()) {
            recalcTotal(0.10);
        } else {
            recalcTotal(null);
        }
    }

    public void changeStatus(OrderStatus newStatus) {
        // прості правила переходів
        switch (status) {
            case NEW:
                if (newStatus != OrderStatus.IN_PROGRESS && newStatus != OrderStatus.CANCELED)
                    throw new InvalidOperationException("З NEW можна перейти лише в IN_PROGRESS або CANCELED");
                break;
            case IN_PROGRESS:
                if (newStatus != OrderStatus.READY && newStatus != OrderStatus.CANCELED)
                    throw new InvalidOperationException("З IN_PROGRESS можна перейти лише в READY або CANCELED");
                break;
            case READY:
                if (newStatus != OrderStatus.COMPLETED && newStatus != OrderStatus.CANCELED)
                    throw new InvalidOperationException("З READY можна перейти лише в COMPLETED або CANCELED");
                break;
            case COMPLETED:
            case CANCELED:
                throw new InvalidOperationException("Замовлення у фінальному статусі — зміна статусу неможлива");
        }
        status = newStatus;
    }

    private void recalcTotal(Double discountPercent) {
        double sum = 0.0;
        for (OrderItem oi : items) sum += oi.lineTotal();
        if (discountPercent != null && discountPercent > 0) {
            sum = sum * (1.0 - discountPercent);
        }
        totalPrice = round2(sum);
    }

    private static double round2(double x) { return Math.round(x * 100.0) / 100.0; }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Order{%s, status=%s, total=%.2f}", id.toString().substring(0,8), status, totalPrice));
        for (OrderItem oi : items) sb.append("\n  - ").append(oi);
        return sb.toString();
    }
}

/* ===================== РЕСТОРАН ===================== */
class Restaurant {
    private final Map<UUID, MenuItem> menu = new LinkedHashMap<>();
    private final Map<UUID, Order> orders = new LinkedHashMap<>();
    private final Map<UUID, Customer> customers = new LinkedHashMap<>();

    /* --- Меню --- */
    public void addMenuItem(MenuItem mi) {
        if (mi == null) throw new IllegalArgumentException("MenuItem не може бути null");
        if (menu.containsKey(mi.getId())) throw new IllegalArgumentException("Позиція меню вже існує");
        menu.put(mi.getId(), mi);
        System.out.println("[MENU] Додано: " + mi);
    }

    public void setAvailability(UUID menuItemId, boolean available) {
        MenuItem m = requireMenuItem(menuItemId);
        m.setAvailable(available);
        System.out.printf("[MENU] %s -> %s%n", m.getName(), available ? "available" : "off");
    }

    public Collection<MenuItem> listMenu() { return Collections.unmodifiableCollection(menu.values()); }

    /* --- Клієнти --- */
    public void addCustomer(Customer c) {
        if (customers.containsKey(c.getId())) throw new IllegalArgumentException("Клієнт уже існує");
        customers.put(c.getId(), c);
        System.out.println("[CUSTOMER] Зареєстровано: " + c.getName());
    }

    public Customer getCustomer(UUID id) {
        Customer c = customers.get(id);
        if (c == null) throw new IllegalArgumentException("Клієнта не знайдено");
        return c;
    }

    /* --- Замовлення --- */
    public Order createOrder(UUID customerId) {
        if (!customers.containsKey(customerId)) throw new IllegalArgumentException("Клієнта не знайдено");
        Order o = new Order(UUID.randomUUID(), customerId);
        orders.put(o.getId(), o);
        System.out.println("[ORDER] Створено: " + o.getId());
        return o;
    }

    public void addItemToOrder(UUID orderId, UUID menuItemId, int qty) {
        Order o = requireOrder(orderId);
        MenuItem m = requireMenuItem(menuItemId);
        o.addItem(m, qty);
        System.out.printf("[ORDER] %s: + %s x%d (total=%.2f)%n",
                shortId(orderId), m.getName(), qty, o.getTotalPrice());
    }

    public void removeItemFromOrder(UUID orderId, UUID menuItemId, int qty) {
        Order o = requireOrder(orderId);
        o.removeItem(menuItemId, qty);
        System.out.printf("[ORDER] %s: - item(%s) x%d (total=%.2f)%n",
                shortId(orderId), shortId(menuItemId), qty, o.getTotalPrice());
    }

    public void applyDiscounts(UUID orderId) {
        Order o = requireOrder(orderId);
        Customer c = getCustomer(o.getCustomerId());
        o.applyDiscountIfEligible(c);
        System.out.printf("[ORDER] %s: застосовано знижки (total=%.2f)%n",
                shortId(orderId), o.getTotalPrice());
    }

    public void changeOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order o = requireOrder(orderId);
        o.changeStatus(newStatus);
        System.out.printf("[ORDER] %s: статус -> %s%n", shortId(orderId), newStatus);
        // Якщо завершено — оновити статистику клієнта
        if (newStatus == OrderStatus.COMPLETED) {
            getCustomer(o.getCustomerId()).incCompletedOrders();
        }
    }

    public void cancelOrder(UUID orderId) {
        Order o = requireOrder(orderId);
        if (o.getStatus() == OrderStatus.COMPLETED)
            throw new InvalidOperationException("Неможливо скасувати виконане замовлення");
        if (o.getStatus() == OrderStatus.CANCELED)
            throw new InvalidOperationException("Замовлення вже скасоване");
        o.changeStatus(OrderStatus.CANCELED);
        System.out.printf("[ORDER] %s: СКАСОВАНО%n", shortId(orderId));
    }

    public Order getOrder(UUID orderId) {
        return requireOrder(orderId);
    }

    public Collection<Order> listOrders() { return Collections.unmodifiableCollection(orders.values()); }

    /* --- Допоміжні --- */
    private Order requireOrder(UUID orderId) {
        Order o = orders.get(orderId);
        if (o == null) throw new OrderNotFoundException("Замовлення не знайдено: " + orderId);
        return o;
    }

    private MenuItem requireMenuItem(UUID id) {
        MenuItem m = menu.get(id);
        if (m == null) throw new IllegalArgumentException("Позицію меню не знайдено: " + id);
        return m;
    }

    private static String shortId(UUID id) { return id.toString().substring(0,8); }
}

/* ===================== ДЕМО ===================== */
public class RestaurantDemo {
    public static void main(String[] args) {
        Restaurant r = new Restaurant();

        // Меню
        MenuItem soup   = new MenuItem(UUID.randomUUID(), "Tomato Soup", 4.50, Category.STARTER, true);
        MenuItem steak  = new MenuItem(UUID.randomUUID(), "Grilled Steak", 15.90, Category.MAIN, true);
        MenuItem cake   = new MenuItem(UUID.randomUUID(), "Cheese Cake", 5.20, Category.DESSERT, true);
        MenuItem cola   = new MenuItem(UUID.randomUUID(), "Cola", 2.00, Category.DRINK, true);
        MenuItem sushi  = new MenuItem(UUID.randomUUID(), "Sushi Set", 12.00, Category.MAIN, false); // недоступний

        r.addMenuItem(soup);
        r.addMenuItem(steak);
        r.addMenuItem(cake);
        r.addMenuItem(cola);
        r.addMenuItem(sushi);

        // Клієнти
        Customer alice = new Customer(UUID.randomUUID(), "Alice");
        Customer bob   = new Customer(UUID.randomUUID(), "Bob");
        r.addCustomer(alice);
        r.addCustomer(bob);

        // Створення замовлення для Alice
        Order o1 = r.createOrder(alice.getId());
        r.addItemToOrder(o1.getId(), soup.getId(), 2);
        r.addItemToOrder(o1.getId(), steak.getId(), 1);
        r.addItemToOrder(o1.getId(), cola.getId(), 2);
        System.out.println(o1);

        // Спроба додати недоступну страву
        try {
            r.addItemToOrder(o1.getId(), sushi.getId(), 1);
        } catch (ItemUnavailableException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }

        // Редагування: прибрати одну колу
        r.removeItemFromOrder(o1.getId(), cola.getId(), 1);
        System.out.println(o1);

        // Зміна статусів
        r.changeOrderStatus(o1.getId(), OrderStatus.IN_PROGRESS);
        r.changeOrderStatus(o1.getId(), OrderStatus.READY);

        // До завершення — застосуємо знижки (Alice ще НЕ постійна)
        r.applyDiscounts(o1.getId());
        r.changeOrderStatus(o1.getId(), OrderStatus.COMPLETED); // тепер 1-ше завершене

        // Зробимо ще 2 замовлення для Alice, щоб стала постійним клієнтом
        for (int i = 0; i < 2; i++) {
            Order extra = r.createOrder(alice.getId());
            r.addItemToOrder(extra.getId(), steak.getId(), 1);
            r.changeOrderStatus(extra.getId(), OrderStatus.IN_PROGRESS);
            r.changeOrderStatus(extra.getId(), OrderStatus.READY);
            r.applyDiscounts(extra.getId()); // ще без знижки до 3-го завершеного
            r.changeOrderStatus(extra.getId(), OrderStatus.COMPLETED);
        }

        // Тепер Alice — постійний клієнт (>=3 завершених). Нове замовлення зі знижкою 10%.
        Order loyal = r.createOrder(alice.getId());
        r.addItemToOrder(loyal.getId(), steak.getId(), 1);
        r.addItemToOrder(loyal.getId(), cake.getId(), 1);
        r.applyDiscounts(loyal.getId()); // застосує знижку
        System.out.println("\n--- Замовлення для постійного клієнта (зі знижкою) ---");
        System.out.println(loyal);

        // Скасування замовлення Bob'а
        Order o2 = r.createOrder(bob.getId());
        r.addItemToOrder(o2.getId(), soup.getId(), 1);
        r.cancelOrder(o2.getId());
        try {
            // Спроба редагувати після скасування
            r.addItemToOrder(o2.getId(), cola.getId(), 1);
        } catch (OrderNotEditableException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }

        // Зробимо страву недоступною і спробуємо додати — має впасти з помилкою
        r.setAvailability(steak.getId(), false);
        Order o3 = r.createOrder(bob.getId());
        try {
            r.addItemToOrder(o3.getId(), steak.getId(), 1);
        } catch (ItemUnavailableException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }

        // Фінальний список замовлень
        System.out.println("\n=== Усі замовлення ===");
        for (Order o : r.listOrders()) {
            System.out.println(o);
            System.out.println();
        }
    }
}