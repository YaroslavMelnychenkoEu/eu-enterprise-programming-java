/**
 * Практичне завдання №3: Імплементація кастомних узагальнених структур даних
 * 
 * Мета: Опанувати принципи створення власних узагальнених структур даних,
 * реалізувати базові операції колекцій та забезпечити типову безпеку при роботі з даними.
 * 
 * @author Student
 * @version 1.0
 */

import java.util.*;
import java.util.function.Predicate;

/**
 * Базовий інтерфейс для всіх колекцій
 * @param <E> тип елементів колекції
 */
interface CustomCollection<E> {
    int size();
    boolean isEmpty();
    boolean contains(Object o);
    boolean add(E e);
    boolean remove(Object o);
    void clear();
    Object[] toArray();
    <T> T[] toArray(T[] a);
}

/**
 * Інтерфейс для ітерованих об'єктів
 * @param <E> тип елементів
 */
interface CustomIterable<E> {
    CustomIterator<E> iterator();
}

/**
 * Інтерфейс ітератора
 * @param <E> тип елементів
 */
interface CustomIterator<E> {
    boolean hasNext();
    E next();
    void remove();
}

/**
 * Узагальнений клас для зберігання елементів з обмеженнями типів
 * @param <T> тип елементів, що повинен реалізовувати Comparable
 */
class BoundedGenericContainer<T extends Comparable<T>> {
    private T value;
    
    public BoundedGenericContainer(T value) {
        this.value = value;
    }
    
    public T getValue() {
        return value;
    }
    
    public void setValue(T value) {
        this.value = value;
    }
    
    public int compareTo(BoundedGenericContainer<T> other) {
        return this.value.compareTo(other.value);
    }
}

/**
 * Кастомна реалізація ArrayList з узагальненнями
 * @param <E> тип елементів списку
 */
public class CustomGenericDataStructures<E> implements CustomCollection<E>, Iterable<E> {
    
    // Внутрішній масив для зберігання елементів
    private Object[] elements;
    private int size;
    private static final int DEFAULT_CAPACITY = 10;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    
    /**
     * Конструктор з початковою ємністю
     */
    public CustomGenericDataStructures(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        this.elements = new Object[initialCapacity];
        this.size = 0;
    }
    
    /**
     * Конструктор за замовчуванням
     */
    public CustomGenericDataStructures() {
        this(DEFAULT_CAPACITY);
    }
    
    /**
     * Конструктор з існуючою колекцією
     */
    public CustomGenericDataStructures(Collection<? extends E> c) {
        this();
        addAll(c);
    }
    
    /**
     * Повертає кількість елементів
     */
    @Override
    public int size() {
        return size;
    }
    
    /**
     * Перевіряє чи колекція порожня
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Перевіряє чи містить колекція вказаний елемент
     */
    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }
    
    /**
     * Додає елемент до колекції
     */
    @Override
    public boolean add(E e) {
        ensureCapacity(size + 1);
        elements[size++] = e;
        return true;
    }
    
    /**
     * Видаляє елемент з колекції
     */
    @Override
    public boolean remove(Object o) {
        int index = indexOf(o);
        if (index >= 0) {
            removeAt(index);
            return true;
        }
        return false;
    }
    
    /**
     * Очищає колекцію
     */
    @Override
    public void clear() {
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }
        size = 0;
    }
    
    /**
     * Повертає масив об'єктів
     */
    @Override
    public Object[] toArray() {
        return Arrays.copyOf(elements, size);
    }
    
    /**
     * Повертає масив вказаного типу
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            return (T[]) Arrays.copyOf(elements, size, a.getClass());
        }
        System.arraycopy(elements, 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }
    
    /**
     * Додає всі елементи з колекції
     */
    public boolean addAll(Collection<? extends E> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacity(size + numNew);
        System.arraycopy(a, 0, elements, size, numNew);
        size += numNew;
        return numNew != 0;
    }
    
    /**
     * Повертає елемент за індексом
     */
    @SuppressWarnings("unchecked")
    public E get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return (E) elements[index];
    }
    
    /**
     * Встановлює елемент за індексом
     */
    @SuppressWarnings("unchecked")
    public E set(int index, E element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        E oldValue = (E) elements[index];
        elements[index] = element;
        return oldValue;
    }
    
    /**
     * Додає елемент за індексом
     */
    public void add(int index, E element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        ensureCapacity(size + 1);
        System.arraycopy(elements, index, elements, index + 1, size - index);
        elements[index] = element;
        size++;
    }
    
    /**
     * Видаляє елемент за індексом
     */
    @SuppressWarnings("unchecked")
    public E removeAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        E oldValue = (E) elements[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elements, index + 1, elements, index, numMoved);
        }
        elements[--size] = null;
        return oldValue;
    }
    
    /**
     * Знаходить індекс елемента
     */
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++) {
                if (elements[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (o.equals(elements[i])) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * Знаходить останній індекс елемента
     */
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size - 1; i >= 0; i--) {
                if (elements[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                if (o.equals(elements[i])) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * Перевіряє чи містить всі елементи з колекції
     */
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Видаляє всі елементи з колекції
     */
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object e : c) {
            if (remove(e)) {
                modified = true;
            }
        }
        return modified;
    }
    
    /**
     * Залишає тільки елементи з колекції
     */
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        for (int i = 0; i < size; i++) {
            if (!c.contains(elements[i])) {
                removeAt(i);
                i--;
                modified = true;
            }
        }
        return modified;
    }
    
    /**
     * Забезпечує достатню ємність
     */
    private void ensureCapacity(int minCapacity) {
        if (minCapacity > elements.length) {
            grow(minCapacity);
        }
    }
    
    /**
     * Збільшує ємність масиву
     */
    private void grow(int minCapacity) {
        int oldCapacity = elements.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            newCapacity = hugeCapacity(minCapacity);
        }
        elements = Arrays.copyOf(elements, newCapacity);
    }
    
    /**
     * Обчислює максимальну ємність
     */
    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) {
            throw new OutOfMemoryError();
        }
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }
    
    /**
     * Повертає ітератор
     */
    @Override
    public Iterator<E> iterator() {
        return new StandardIteratorImpl();
    }
    
    /**
     * Внутрішня реалізація стандартного ітератора
     */
    private class StandardIteratorImpl implements Iterator<E> {
        int cursor = 0;
        int lastRet = -1;
        
        @Override
        public boolean hasNext() {
            return cursor != size;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (cursor >= size) {
                throw new NoSuchElementException();
            }
            lastRet = cursor;
            return (E) elements[cursor++];
        }
        
        @Override
        public void remove() {
            if (lastRet < 0) {
                throw new IllegalStateException();
            }
            CustomGenericDataStructures.this.removeAt(lastRet);
            cursor = lastRet;
            lastRet = -1;
        }
    }
    
    /**
     * Повертає рядкове представлення колекції
     */
    @Override
    public String toString() {
        if (size == 0) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < size; i++) {
            sb.append(elements[i]);
            if (i < size - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
        return sb.toString();
    }
    
    /**
     * Перевіряє рівність об'єктів
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof CustomGenericDataStructures)) {
            return false;
        }
        
        CustomGenericDataStructures<?> other = (CustomGenericDataStructures<?>) o;
        if (size != other.size) {
            return false;
        }
        
        for (int i = 0; i < size; i++) {
            if (!Objects.equals(elements[i], other.elements[i])) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Повертає хеш-код
     */
    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < size; i++) {
            result = 31 * result + (elements[i] == null ? 0 : elements[i].hashCode());
        }
        return result;
    }
    
    /**
     * Сортує елементи за допомогою Comparator
     * @param c компаратор для порівняння елементів
     */
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) {
        if (size <= 1) {
            return;
        }
        
        // Використовуємо алгоритм сортування злиттям (merge sort)
        mergeSort(0, size - 1, c);
    }
    
    /**
     * Сортує елементи, якщо вони реалізують Comparable
     */
    @SuppressWarnings("unchecked")
    public void sort() {
        if (size <= 1) {
            return;
        }
        
        // Перевіряємо чи всі елементи реалізують Comparable
        for (int i = 0; i < size; i++) {
            if (elements[i] != null && !(elements[i] instanceof Comparable)) {
                throw new ClassCastException("Elements must implement Comparable for natural sorting");
            }
        }
        
        // Використовуємо природне сортування
        sort((Comparator<? super E>) Comparator.naturalOrder());
    }
    
    /**
     * Реалізація алгоритму сортування злиттям
     */
    @SuppressWarnings("unchecked")
    private void mergeSort(int left, int right, Comparator<? super E> c) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            
            // Рекурсивно сортуємо ліву та праву частини
            mergeSort(left, mid, c);
            mergeSort(mid + 1, right, c);
            
            // Зливаємо відсортовані частини
            merge(left, mid, right, c);
        }
    }
    
    /**
     * Зливає дві відсортовані частини масиву
     */
    @SuppressWarnings("unchecked")
    private void merge(int left, int mid, int right, Comparator<? super E> c) {
        // Створюємо тимчасові масиви для лівої та правої частин
        Object[] leftArray = new Object[mid - left + 1];
        Object[] rightArray = new Object[right - mid];
        
        // Копіюємо дані в тимчасові масиви
        System.arraycopy(elements, left, leftArray, 0, leftArray.length);
        System.arraycopy(elements, mid + 1, rightArray, 0, rightArray.length);
        
        // Індекси для лівої, правої та основного масивів
        int leftIndex = 0, rightIndex = 0, mainIndex = left;
        
        // Зливаємо тимчасові масиви назад в основний масив
        while (leftIndex < leftArray.length && rightIndex < rightArray.length) {
            E leftElement = (E) leftArray[leftIndex];
            E rightElement = (E) rightArray[rightIndex];
            
            if (c.compare(leftElement, rightElement) <= 0) {
                elements[mainIndex] = leftArray[leftIndex];
                leftIndex++;
            } else {
                elements[mainIndex] = rightArray[rightIndex];
                rightIndex++;
            }
            mainIndex++;
        }
        
        // Копіюємо залишкові елементи з лівої частини
        while (leftIndex < leftArray.length) {
            elements[mainIndex] = leftArray[leftIndex];
            leftIndex++;
            mainIndex++;
        }
        
        // Копіюємо залишкові елементи з правої частини
        while (rightIndex < rightArray.length) {
            elements[mainIndex] = rightArray[rightIndex];
            rightIndex++;
            mainIndex++;
        }
    }
    
    /**
     * Сортує елементи в зворотному порядку
     */
    @SuppressWarnings("unchecked")
    public void sortReverse() {
        if (size <= 1) {
            return;
        }
        
        // Перевіряємо чи всі елементи реалізують Comparable
        for (int i = 0; i < size; i++) {
            if (elements[i] != null && !(elements[i] instanceof Comparable)) {
                throw new ClassCastException("Elements must implement Comparable for reverse sorting");
            }
        }
        
        // Використовуємо зворотне сортування
        sort((Comparator<? super E>) Comparator.reverseOrder());
    }
    
    /**
     * Знаходить мінімальний елемент
     */
    @SuppressWarnings("unchecked")
    public E min(Comparator<? super E> c) {
        if (isEmpty()) {
            throw new NoSuchElementException("Collection is empty");
        }
        
        E min = (E) elements[0];
        for (int i = 1; i < size; i++) {
            E current = (E) elements[i];
            if (c.compare(current, min) < 0) {
                min = current;
            }
        }
        return min;
    }
    
    /**
     * Знаходить максимальний елемент
     */
    @SuppressWarnings("unchecked")
    public E max(Comparator<? super E> c) {
        if (isEmpty()) {
            throw new NoSuchElementException("Collection is empty");
        }
        
        E max = (E) elements[0];
        for (int i = 1; i < size; i++) {
            E current = (E) elements[i];
            if (c.compare(current, max) > 0) {
                max = current;
            }
        }
        return max;
    }
    
    /**
     * Знаходить мінімальний елемент (для Comparable)
     */
    @SuppressWarnings("unchecked")
    public E min() {
        if (isEmpty()) {
            throw new NoSuchElementException("Collection is empty");
        }
        
        // Перевіряємо чи всі елементи реалізують Comparable
        for (int i = 0; i < size; i++) {
            if (elements[i] != null && !(elements[i] instanceof Comparable)) {
                throw new ClassCastException("Elements must implement Comparable for natural ordering");
            }
        }
        
        return min((Comparator<? super E>) Comparator.naturalOrder());
    }
    
    /**
     * Знаходить максимальний елемент (для Comparable)
     */
    @SuppressWarnings("unchecked")
    public E max() {
        if (isEmpty()) {
            throw new NoSuchElementException("Collection is empty");
        }
        
        // Перевіряємо чи всі елементи реалізують Comparable
        for (int i = 0; i < size; i++) {
            if (elements[i] != null && !(elements[i] instanceof Comparable)) {
                throw new ClassCastException("Elements must implement Comparable for natural ordering");
            }
        }
        
        return max((Comparator<? super E>) Comparator.naturalOrder());
    }
    
    /**
     * Демонстраційний метод для показу роботи з узагальненнями
     */
    public static void demonstrateGenerics() {
        System.out.println("=== Демонстрація узагальнень (Generics) ===");
        
        // Створення колекції для рядків
        CustomGenericDataStructures<String> stringList = new CustomGenericDataStructures<>();
        stringList.add("Привіт");
        stringList.add("Світ");
        stringList.add("Java");
        
        System.out.println("Список рядків: " + stringList);
        
        // Створення колекції для чисел
        CustomGenericDataStructures<Integer> intList = new CustomGenericDataStructures<>();
        intList.add(1);
        intList.add(2);
        intList.add(3);
        
        System.out.println("Список чисел: " + intList);
        
        // Демонстрація обмежень типів
        BoundedGenericContainer<String> stringContainer = new BoundedGenericContainer<>("Test");
        BoundedGenericContainer<Integer> intContainer = new BoundedGenericContainer<>(42);
        
        System.out.println("Контейнер рядків: " + stringContainer.getValue());
        System.out.println("Контейнер чисел: " + intContainer.getValue());
        
        // Порівняння контейнерів
        System.out.println("Порівняння: " + stringContainer.compareTo(new BoundedGenericContainer<>("Test")));
    }
    
    /**
     * Демонстраційний метод для показу роботи з ітерацією
     */
    public static void demonstrateIteration() {
        System.out.println("\n=== Демонстрація ітерації ===");
        
        CustomGenericDataStructures<String> list = new CustomGenericDataStructures<>();
        list.add("Перший");
        list.add("Другий");
        list.add("Третій");
        
        System.out.println("Ітерація через for-each (через ітератор):");
        for (String item : list) {
            System.out.println("- " + item);
        }
        
        System.out.println("\nІтерація через ітератор з видаленням:");
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            if (item.equals("Другий")) {
                iterator.remove();
                System.out.println("Видалено: " + item);
            } else {
                System.out.println("Залишено: " + item);
            }
        }
        
        System.out.println("Результат: " + list);
    }
    
    /**
     * Демонстраційний метод для показу типобезпеки
     */
    public static void demonstrateTypeSafety() {
        System.out.println("\n=== Демонстрація типобезпеки ===");
        
        // Типобезпека на рівні компіляції
        CustomGenericDataStructures<String> stringList = new CustomGenericDataStructures<>();
        stringList.add("Безпечний рядок");
        // stringList.add(123); // Це викличе помилку компіляції!
        
        CustomGenericDataStructures<Integer> intList = new CustomGenericDataStructures<>();
        intList.add(123);
        // intList.add("Рядок"); // Це викличе помилку компіляції!
        
        System.out.println("Список рядків: " + stringList);
        System.out.println("Список чисел: " + intList);
        
        // Безпечне отримання елементів
        String firstString = stringList.get(0); // Не потрібно приведення типів!
        Integer firstInt = intList.get(0); // Не потрібно приведення типів!
        
        System.out.println("Перший рядок: " + firstString);
        System.out.println("Перше число: " + firstInt);
    }
    
    /**
     * Демонстраційний метод для показу інкапсуляції
     */
    public static void demonstrateEncapsulation() {
        System.out.println("\n=== Демонстрація інкапсуляції ===");
        
        CustomGenericDataStructures<String> list = new CustomGenericDataStructures<>();
        list.add("Приватний");
        list.add("Захищений");
        list.add("Публічний");
        
        System.out.println("Розмір колекції: " + list.size());
        System.out.println("Чи порожня: " + list.isEmpty());
        System.out.println("Містить 'Приватний': " + list.contains("Приватний"));
        
        // Внутрішній стан захищений від прямого доступу
        // list.elements; // Недоступно ззовні!
        // list.size = -1; // Недоступно ззовні!
        
        System.out.println("Колекція: " + list);
    }
    
    /**
     * Демонстраційний метод для показу сортування
     */
    public static void demonstrateSorting() {
        System.out.println("\n=== Демонстрація сортування ===");
        
        // Сортування рядків
        CustomGenericDataStructures<String> stringList = new CustomGenericDataStructures<>();
        stringList.add("Яблуко");
        stringList.add("Банан");
        stringList.add("Апельсин");
        stringList.add("Груша");
        stringList.add("Вишня");
        
        System.out.println("Початковий список рядків: " + stringList);
        stringList.sort();
        System.out.println("Після сортування (алфавітний порядок): " + stringList);
        stringList.sortReverse();
        System.out.println("Після зворотного сортування: " + stringList);
        
        // Сортування чисел
        CustomGenericDataStructures<Integer> intList = new CustomGenericDataStructures<>();
        intList.add(42);
        intList.add(15);
        intList.add(7);
        intList.add(99);
        intList.add(3);
        
        System.out.println("\nПочатковий список чисел: " + intList);
        intList.sort();
        System.out.println("Після сортування (зростання): " + intList);
        intList.sortReverse();
        System.out.println("Після зворотного сортування (спадання): " + intList);
        
        // Знаходження мінімального та максимального елементів
        System.out.println("Мінімальний елемент: " + intList.min());
        System.out.println("Максимальний елемент: " + intList.max());
        
        // Сортування з кастомним компаратором
        CustomGenericDataStructures<String> customList = new CustomGenericDataStructures<>();
        customList.add("короткий");
        customList.add("дуже довгий рядок");
        customList.add("середній");
        customList.add("а");
        
        System.out.println("\nСписок для сортування за довжиною: " + customList);
        customList.sort(Comparator.comparing(String::length));
        System.out.println("Після сортування за довжиною: " + customList);
        
        // Сортування об'єктів Person
        CustomGenericDataStructures<Person> personList = new CustomGenericDataStructures<>();
        personList.add(new Person("Анна", 25));
        personList.add(new Person("Борис", 30));
        personList.add(new Person("Віктор", 20));
        personList.add(new Person("Галина", 35));
        
        System.out.println("\nСписок осіб: " + personList);
        personList.sort(Comparator.comparing(Person::getName));
        System.out.println("Після сортування за іменем: " + personList);
        personList.sort(Comparator.comparing(Person::getAge));
        System.out.println("Після сортування за віком: " + personList);
        
        // Демонстрація помилки з несумісними типами
        try {
            CustomGenericDataStructures<Object> objectList = new CustomGenericDataStructures<>();
            objectList.add("Рядок");
            objectList.add(123);
            objectList.add(new Object());
            System.out.println("\nСписок з різними типами: " + objectList);
            objectList.sort(); // Це викличе помилку
        } catch (ClassCastException e) {
            System.out.println("Очікувана помилка при сортуванні різних типів: " + e.getMessage());
        }
    }
    
    /**
     * Головний метод для демонстрації всіх можливостей
     */
    public static void main(String[] args) {
        System.out.println("Практичне завдання №3: Імплементація кастомних узагальнених структур даних");
        System.out.println("========================================================================");
        
        try {
            demonstrateGenerics();
            demonstrateIteration();
            demonstrateTypeSafety();
            demonstrateEncapsulation();
            demonstrateSorting();
            
            System.out.println("\n=== Додаткові операції ===");
            
            // Демонстрація роботи з масивами
            CustomGenericDataStructures<String> list = new CustomGenericDataStructures<>();
            list.add("Елемент1");
            list.add("Елемент2");
            list.add("Елемент3");
            
            // Конвертація в масив
            String[] array = list.toArray(new String[0]);
            System.out.println("Масив: " + Arrays.toString(array));
            
            // Додавання за індексом
            list.add(1, "Новий елемент");
            System.out.println("Після додавання за індексом: " + list);
            
            // Заміна елемента
            String oldValue = list.set(2, "Замінений елемент");
            System.out.println("Замінено '" + oldValue + "' на 'Замінений елемент'");
            System.out.println("Результат: " + list);
            
            // Видалення за індексом
            String removed = list.removeAt(0);
            System.out.println("Видалено: " + removed);
            System.out.println("Результат: " + list);
            
            System.out.println("\n=== Тестування з різними типами ===");
            
            // Робота з числами
            CustomGenericDataStructures<Double> doubleList = new CustomGenericDataStructures<>();
            doubleList.add(3.14);
            doubleList.add(2.71);
            doubleList.add(1.41);
            
            System.out.println("Список чисел з плаваючою точкою: " + doubleList);
            
            // Робота з об'єктами
            CustomGenericDataStructures<Person> personList = new CustomGenericDataStructures<>();
            personList.add(new Person("Іван", 25));
            personList.add(new Person("Марія", 30));
            personList.add(new Person("Петро", 35));
            
            System.out.println("Список осіб: " + personList);
            
        } catch (Exception e) {
            System.err.println("Помилка: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Допоміжний клас для демонстрації
     */
    static class Person {
        private String name;
        private int age;
        
        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        public String getName() {
            return name;
        }
        
        public int getAge() {
            return age;
        }
        
        @Override
        public String toString() {
            return name + "(" + age + ")";
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return age == person.age && Objects.equals(name, person.name);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }
}
