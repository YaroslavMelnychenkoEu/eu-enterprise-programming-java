import java.util.*;

/** ====== ВИНЯТКИ ====== */
class BookAlreadyLentException extends RuntimeException {
    public BookAlreadyLentException(String msg) { super(msg); }
}
class TooManyBooksException extends RuntimeException {
    public TooManyBooksException(String msg) { super(msg); }
}
class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String msg) { super(msg); }
}
class ReaderNotFoundException extends RuntimeException {
    public ReaderNotFoundException(String msg) { super(msg); }
}
class BookNotOwnedByReaderException extends RuntimeException {
    public BookNotOwnedByReaderException(String msg) { super(msg); }
}

/** ====== МОДЕЛІ ====== */
class Book {
    private final UUID id;
    private final String title;
    private final String author;
    private final int year;
    private boolean available;

    public Book(UUID id, String title, String author, int year, boolean available) {
        if (id == null) throw new IllegalArgumentException("Book id не може бути null");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Назва не може бути порожньою");
        if (author == null || author.isBlank()) throw new IllegalArgumentException("Автор не може бути порожнім");
        if (year < 0) throw new IllegalArgumentException("Рік має бути невідʼємним");
        this.id = id;
        this.title = title.trim();
        this.author = author.trim();
        this.year = year;
        this.available = available;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }
    public boolean isAvailable() { return available; }
    void setAvailable(boolean available) { this.available = available; }

    @Override public String toString() {
        return String.format("Book{%s, \"%s\" by %s, %d, %s}",
                id.toString().substring(0,8), title, author, year, available ? "available" : "lent");
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        return id.equals(((Book)o).id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}

class Reader {
    private final UUID id;
    private final String name;
    private final List<Book> books = new ArrayList<>();

    public Reader(UUID id, String name) {
        if (id == null) throw new IllegalArgumentException("Reader id не може бути null");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Імʼя не може бути порожнім");
        this.id = id;
        this.name = name.trim();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public List<Book> getBooks() { return Collections.unmodifiableList(books); }

    /* пакетні методи для Library */
    void _take(Book b) { books.add(b); }
    void _return(Book b) { books.remove(b); }

    @Override public String toString() {
        return String.format("Reader{%s, %s, borrowed=%d}", id.toString().substring(0,8), name, books.size());
    }
}

/** ====== БІБЛІОТЕКА ====== */
class Library {
    private final Map<UUID, Book> books = new HashMap<>();
    private final Map<UUID, Reader> readers = new HashMap<>();
    private final Map<UUID, UUID> bookToReader = new HashMap<>(); // bookId -> readerId
    private final int maxBooksPerReader;

    public Library(int maxBooksPerReader) {
        if (maxBooksPerReader <= 0) throw new IllegalArgumentException("Ліміт має бути > 0");
        this.maxBooksPerReader = maxBooksPerReader;
    }

    public void addBook(Book book) {
        if (book == null) throw new IllegalArgumentException("Book не може бути null");
        if (books.containsKey(book.getId()))
            throw new IllegalArgumentException("Така книга вже існує в бібліотеці");
        books.put(book.getId(), book);
        System.out.println("[INFO] Додано: " + book);
    }

    public void removeBook(Book book) {
        if (book == null) throw new IllegalArgumentException("Book не може бути null");
        Book stored = books.get(book.getId());
        if (stored == null) throw new BookNotFoundException("Книга не існує в бібліотеці");
        if (!stored.isAvailable())
            throw new IllegalStateException("Не можна видалити видану книгу");
        books.remove(book.getId());
        System.out.println("[INFO] Видалено з каталогу: " + stored);
    }

    public void addReader(Reader reader) {
        if (reader == null) throw new IllegalArgumentException("Reader не може бути null");
        if (readers.containsKey(reader.getId()))
            throw new IllegalArgumentException("Такий читач уже зареєстрований");
        readers.put(reader.getId(), reader);
        System.out.println("[INFO] Зареєстровано читача: " + reader);
    }

    public void lendBook(Book book, Reader reader) {
        Book b = getBookOrThrow(book);
        Reader r = getReaderOrThrow(reader);

        if (!b.isAvailable()) throw new BookAlreadyLentException("Книга вже видана");
        if (r.getBooks().size() >= maxBooksPerReader)
            throw new TooManyBooksException("Читач має забагато книг (ліміт: " + maxBooksPerReader + ")");

        b.setAvailable(false);
        r._take(b);
        bookToReader.put(b.getId(), r.getId());
        System.out.printf("[INFO] Видано книгу \"%s\" читачеві %s%n", b.getTitle(), r.getName());
    }

    public void returnBook(Book book, Reader reader) {
        Book b = getBookOrThrow(book);
        Reader r = getReaderOrThrow(reader);

        if (!bookToReader.containsKey(b.getId()))
            throw new BookNotFoundException("Книга не значиться як видана");
        UUID actualReaderId = bookToReader.get(b.getId());
        if (!actualReaderId.equals(r.getId()))
            throw new BookNotOwnedByReaderException("Ця книга видана іншому читачеві");

        b.setAvailable(true);
        r._return(b);
        bookToReader.remove(b.getId());
        System.out.printf("[INFO] Повернення книги \"%s\" від читача %s%n", b.getTitle(), r.getName());
    }

    public Optional<Reader> whoHas(Book book) {
        Book b = getBookOrThrow(book);
        UUID rid = bookToReader.get(b.getId());
        return rid == null ? Optional.empty() : Optional.of(readers.get(rid));
    }

    public Collection<Book> listBooks() { return Collections.unmodifiableCollection(books.values()); }
    public Collection<Reader> listReaders() { return Collections.unmodifiableCollection(readers.values()); }

    private Book getBookOrThrow(Book book) {
        Book stored = books.get(book.getId());
        if (stored == null) throw new BookNotFoundException("Книга не існує в бібліотеці");
        return stored;
    }
    private Reader getReaderOrThrow(Reader reader) {
        Reader stored = readers.get(reader.getId());
        if (stored == null) throw new ReaderNotFoundException("Читача не знайдено");
        return stored;
    }
}

/** ====== ДЕМО ====== */
public class LibraryDemo {
    public static void main(String[] args) {
        Library lib = new Library(2); // максимум 2 книги на читача

        // Книги
        Book b1 = new Book(UUID.randomUUID(), "Clean Code", "Robert C. Martin", 2008, true);
        Book b2 = new Book(UUID.randomUUID(), "Effective Java", "Joshua Bloch", 2018, true);
        Book b3 = new Book(UUID.randomUUID(), "The Pragmatic Programmer", "Andrew Hunt", 1999, true);

        lib.addBook(b1);
        lib.addBook(b2);
        lib.addBook(b3);

        // Читачі
        Reader r1 = new Reader(UUID.randomUUID(), "Alice");
        Reader r2 = new Reader(UUID.randomUUID(), "Bob");

        lib.addReader(r1);
        lib.addReader(r2);

        System.out.println("\n--- Каталог ---");
        lib.listBooks().forEach(System.out::println);

        // Видача
        lib.lendBook(b1, r1);
        lib.lendBook(b2, r1);

        // Спроба перевищити ліміт
        try {
            lib.lendBook(b3, r1);
        } catch (TooManyBooksException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }

        // Спроба видати вже видану книгу
        try {
            lib.lendBook(b1, r2);
        } catch (BookAlreadyLentException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }

        System.out.println("\nХто має \"Clean Code\"? -> " +
                lib.whoHas(b1).map(Reader::getName).orElse("ніхто"));

        // Повернення
        lib.returnBook(b1, r1);

        // Спроба повернути книгою не тим читачем
        try {
            lib.returnBook(b2, r2);
        } catch (BookNotOwnedByReaderException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }

        System.out.println("\n--- Фінальний каталог ---");
        lib.listBooks().forEach(System.out::println);

        // Видалення доступної книги
        lib.removeBook(b1);

        // Спроба видалити неіснуючу
        try {
            lib.removeBook(b1);
        } catch (BookNotFoundException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }
    }
}
