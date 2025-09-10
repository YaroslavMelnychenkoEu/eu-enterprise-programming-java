import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** ==== Довідкові типи ==== */
enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
enum Status { TODO, IN_PROGRESS, BLOCKED, DONE }

/** ==== Сервіс сповіщень ==== */
interface NotificationService {
    void notify(User recipient, String message);
    void broadcast(Collection<User> recipients, String message);
}

class ConsoleNotificationService implements NotificationService {
    @Override public void notify(User recipient, String message) {
        System.out.printf("[NOTIFY -> %s] %s%n", recipient.getName(), message);
    }
    @Override public void broadcast(Collection<User> recipients, String message) {
        for (User u : recipients) notify(u, message);
    }
}

/** ==== Модель користувача ==== */
class User {
    private final UUID id;
    private final String name;
    private final List<Task> tasks = new ArrayList<>();

    public User(UUID id, String name) {
        if (id == null) throw new IllegalArgumentException("User id не може бути null");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("User name не може бути порожнім");
        this.id = id;
        this.name = name.trim();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public List<Task> getTasks() { return Collections.unmodifiableList(tasks); }

    /* пакетний доступ для Project */
    void _attachTask(Task t) { tasks.add(t); }
    void _detachTask(Task t) { tasks.remove(t); }

    @Override public String toString() {
        return "User{" + "id=" + id + ", name='" + name + '\'' + ", tasks=" + tasks.size() + '}';
    }
}

/** ==== Модель задачі ==== */
class Task {
    private final UUID id;
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    private LocalDateTime deadline; // може бути null
    private User assignee;

    public Task(UUID id, String title, String description, Priority priority, Status status, LocalDateTime deadline) {
        if (id == null) throw new IllegalArgumentException("Task id не може бути null");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Task title не може бути порожнім");
        if (priority == null) throw new IllegalArgumentException("Task priority не може бути null");
        if (status == null) throw new IllegalArgumentException("Task status не може бути null");
        this.id = id;
        this.title = title.trim();
        this.description = (description == null) ? "" : description.trim();
        this.priority = priority;
        this.status = status;
        this.deadline = deadline;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Priority getPriority() { return priority; }
    public Status getStatus() { return status; }
    public LocalDateTime getDeadline() { return deadline; }
    public Optional<User> getAssignee() { return Optional.ofNullable(assignee); }

    void setAssignee(User assignee) { this.assignee = assignee; }
    void setStatus(Status status) { this.status = status; }
    void setPriority(Priority priority) { this.priority = priority; }
    void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public boolean isOverdue() {
        return deadline != null && LocalDateTime.now().isAfter(deadline) && status != Status.DONE;
    }

    public String shortView() {
        String d = (deadline == null) ? "—"
                : deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return String.format("#%s | %s | %s | %s | %s | due: %s",
                id.toString().substring(0, 8), title, priority, status,
                assignee != null ? assignee.getName() : "unassigned", d);
    }

    @Override public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", deadline=" + (deadline == null ? "—" : deadline) +
                ", assignee=" + (assignee == null ? "—" : assignee.getName()) +
                '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        return id.equals(((Task) o).id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}

/** ==== Проєкт і операції управління ==== */
class Project {
    private final String name;
    private final List<User> users = new ArrayList<>();
    private final List<Task> tasks = new ArrayList<>();
    private final NotificationService notifier;

    public Project(String name, NotificationService notifier) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Project name не може бути порожнім");
        this.name = name.trim();
        this.notifier = (notifier != null) ? notifier : new ConsoleNotificationService();
    }

    public String getName() { return name; }
    public List<User> getUsers() { return Collections.unmodifiableList(users); }
    public List<Task> getTasks() { return Collections.unmodifiableList(tasks); }

    public void addUser(User u) {
        if (u == null) throw new IllegalArgumentException("User не може бути null");
        if (users.stream().anyMatch(x -> x.getId().equals(u.getId())))
            throw new IllegalArgumentException("Користувач уже доданий до проєкту");
        users.add(u);
        notifier.broadcast(users, String.format("До проєкту '%s' додано користувача %s", name, u.getName()));
    }

    public void addTask(Task t) {
        if (t == null) throw new IllegalArgumentException("Task не може бути null");
        if (tasks.contains(t)) throw new IllegalArgumentException("Така задача вже є у проєкті");
        tasks.add(t);
        notifier.broadcast(users, String.format("Створено задачу: %s", t.shortView()));
    }

    public void assignTask(UUID taskId, UUID userId) {
        Task t = findTask(taskId);
        User u = findUser(userId);

        t.getAssignee().ifPresent(prev -> prev._detachTask(t));
        t.setAssignee(u);
        u._attachTask(t);

        notifier.notify(u, String.format("Вам призначено задачу: %s", t.shortView()));
        notifier.broadcast(users, String.format("Задачу '%s' призначено користувачеві %s", t.getTitle(), u.getName()));
    }

    public void changeTaskStatus(UUID taskId, Status newStatus) {
        Task t = findTask(taskId);
        Status old = t.getStatus();
        t.setStatus(newStatus);

        // Автопріоритезація: якщо прострочена і не DONE — підняти до CRITICAL
        if (t.isOverdue() && t.getPriority() != Priority.CRITICAL) {
            t.setPriority(Priority.CRITICAL);
        }

        t.getAssignee().ifPresentOrElse(
                u -> notifier.notify(u, String.format("Статус задачі змінено: %s -> %s | %s",
                        old, newStatus, t.shortView())),
                () -> notifier.broadcast(users, String.format("Статус задачі (без виконавця) змінено: %s -> %s | %s",
                        old, newStatus, t.shortView()))
        );
    }

    /** Сортування: пріоритет (CRITICAL..LOW) -> найближчий дедлайн (null в кінець) */
    public void reprioritize() {
        tasks.sort(priorityThenDeadline());
        notifier.broadcast(users, "Виконано перепріоритезацію задач проєкту");
    }

    /** ==== Фільтри ==== */
    public List<Task> filterTasks(Predicate<Task> predicate) {
        return tasks.stream().filter(predicate).collect(Collectors.toList());
    }

    public List<Task> tasksByStatus(Status status) {
        return filterTasks(t -> t.getStatus() == status);
    }

    public List<Task> tasksByPriority(Priority p) {
        return filterTasks(t -> t.getPriority() == p);
    }

    public List<Task> tasksByAssignee(UUID userId) {
        return filterTasks(t -> t.getAssignee().map(a -> a.getId().equals(userId)).orElse(false));
    }

    public List<Task> overdueTasks() {
        return filterTasks(Task::isOverdue);
    }

    public List<Task> dueWithinHours(long hours) {
        LocalDateTime to = LocalDateTime.now().plusHours(hours);
        return filterTasks(t -> t.getDeadline() != null && !t.isOverdue() && !t.getDeadline().isAfter(to));
    }

    /** ==== Допоміжні ==== */
    private Task findTask(UUID taskId) {
        return tasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Задачі з таким id немає в проєкті"));
    }

    private User findUser(UUID userId) {
        return users.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Користувача з таким id немає в проєкті"));
    }

    /** Компаратор для пріоритезації */
    public static Comparator<Task> priorityThenDeadline() {
        return Comparator
                .comparing(Task::getPriority, Comparator.comparingInt(p -> {
                    switch (p) {
                        case CRITICAL: return 0;
                        case HIGH: return 1;
                        case MEDIUM: return 2;
                        default: return 3; // LOW
                    }
                }))
                .thenComparing(t -> Optional.ofNullable(t.getDeadline()).orElse(LocalDateTime.MAX));
    }
}

/** ==== Демонстрація ==== */
public class ProjectDemo {
    public static void main(String[] args) {
        NotificationService notifier = new ConsoleNotificationService();
        Project project = new Project("Task Manager 101", notifier);

        // Користувачі
        User alice = new User(UUID.randomUUID(), "Alice");
        User bob   = new User(UUID.randomUUID(), "Bob");
        project.addUser(alice);
        project.addUser(bob);

        // Задачі
        Task t1 = new Task(UUID.randomUUID(), "Design data model",
                "Спроєктувати класи Task/User/Project", Priority.HIGH, Status.TODO,
                LocalDateTime.now().plusDays(2));

        Task t2 = new Task(UUID.randomUUID(), "Implement filters",
                "Фільтрація за статусом, пріоритетом, виконавцем, дедлайном",
                Priority.MEDIUM, Status.TODO, LocalDateTime.now().plusDays(4));

        Task t3 = new Task(UUID.randomUUID(), "Notifications",
                "Додати сервіс сповіщень та події", Priority.CRITICAL, Status.IN_PROGRESS,
                LocalDateTime.now().plusHours(6));

        Task t4 = new Task(UUID.randomUUID(), "Docs & README",
                "Короткий опис використання", Priority.LOW, Status.TODO,
                LocalDateTime.now().minusHours(3)); // уже прострочено

        project.addTask(t1);
        project.addTask(t2);
        project.addTask(t3);
        project.addTask(t4);

        // Призначення задач
        project.assignTask(t1.getId(), alice.getId());
        project.assignTask(t3.getId(), alice.getId());
        project.assignTask(t2.getId(), bob.getId());
        // t4 залишимо без виконавця

        // Зміна статусів (покаже сповіщення + автопідняття пріоритету для прострочених)
        project.changeTaskStatus(t3.getId(), Status.DONE);
        project.changeTaskStatus(t4.getId(), Status.IN_PROGRESS);

        // Перепріоритезація (пріоритет -> дедлайн)
        project.reprioritize();

        // Вивід списків
        System.out.println("\n--- Усі задачі (після сортування) ---");
        project.getTasks().forEach(t -> System.out.println(t.shortView()));

        System.out.println("\n--- Overdue ---");
        project.overdueTasks().forEach(t -> System.out.println(t.shortView()));

        System.out.println("\n--- Due within 24h ---");
        project.dueWithinHours(24).forEach(t -> System.out.println(t.shortView()));

        System.out.println("\n--- By assignee: Alice ---");
        project.tasksByAssignee(alice.getId()).forEach(t -> System.out.println(t.shortView()));

        System.out.println("\n--- By priority: CRITICAL ---");
        project.tasksByPriority(Priority.CRITICAL).forEach(t -> System.out.println(t.shortView()));

        System.out.println("\n--- By status: TODO ---");
        project.tasksByStatus(Status.TODO).forEach(t -> System.out.println(t.shortView()));
    }
}