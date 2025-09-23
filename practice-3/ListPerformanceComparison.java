/**
 * Практичне завдання №2: Оптимізація операцій з List: ArrayList vs LinkedList
 * 
 * Мета: Порівняти продуктивність ArrayList та LinkedList для різних операцій,
 * провести заміри часу виконання, профілювання пам'яті та оптимізацію критичних операцій.
 * 
 * @author Student
 * @version 1.0
 */

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Клас для тестування продуктивності різних операцій з ArrayList та LinkedList
 */
public class ListPerformanceComparison {
    
    // Константи для тестування
    private static final int SMALL_SIZE = 1000;
    private static final int MEDIUM_SIZE = 10000;
    private static final int LARGE_SIZE = 100000;
    private static final int VERY_LARGE_SIZE = 1000000;
    
    // Результати тестів
    private static final Map<String, List<TestResult>> testResults = new HashMap<>();
    
    /**
     * Клас для зберігання результатів тестування
     */
    static class TestResult {
        private final String operation;
        private final String listType;
        private final int size;
        private final long timeNanos;
        private final long memoryUsed;
        
        public TestResult(String operation, String listType, int size, long timeNanos, long memoryUsed) {
            this.operation = operation;
            this.listType = listType;
            this.size = size;
            this.timeNanos = timeNanos;
            this.memoryUsed = memoryUsed;
        }
        
        public String getOperation() { return operation; }
        public String getListType() { return listType; }
        public int getSize() { return size; }
        public long getTimeNanos() { return timeNanos; }
        public long getMemoryUsed() { return memoryUsed; }
        
        @Override
        public String toString() {
            return String.format("%s %s (size=%d): %d ns, %d bytes", 
                operation, listType, size, timeNanos, memoryUsed);
        }
    }
    
    /**
     * Утилітарний клас для вимірювання пам'яті
     */
    static class MemoryProfiler {
        private static final Runtime runtime = Runtime.getRuntime();
        
        public static long getUsedMemory() {
            return runtime.totalMemory() - runtime.freeMemory();
        }
        
        public static void forceGarbageCollection() {
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public static long measureMemoryUsage(Runnable task) {
            forceGarbageCollection();
            long before = getUsedMemory();
            task.run();
            long after = getUsedMemory();
            return after - before;
        }
    }
    
    /**
     * Утилітарний клас для вимірювання часу
     */
    static class TimeProfiler {
        public static long measureTime(Runnable task) {
            long startTime = System.nanoTime();
            task.run();
            long endTime = System.nanoTime();
            return endTime - startTime;
        }
        
        public static long measureTimeWithWarmup(Runnable task, int warmupRuns) {
            // Прогрів JVM
            for (int i = 0; i < warmupRuns; i++) {
                task.run();
            }
            
            // Вимірювання
            return measureTime(task);
        }
    }
    
    /**
     * Тест додавання елементів в кінець списку
     */
    public static void testAddToEnd() {
        System.out.println("\n=== Тест додавання в кінець списку ===");
        
        int[] sizes = {SMALL_SIZE, MEDIUM_SIZE, LARGE_SIZE};
        
        for (int size : sizes) {
            // ArrayList
            List<Integer> arrayList = new ArrayList<>();
            long arrayListTime = TimeProfiler.measureTimeWithWarmup(() -> {
                for (int i = 0; i < size; i++) {
                    arrayList.add(i);
                }
            }, 3);
            
            long arrayListMemory = MemoryProfiler.measureMemoryUsage(() -> {
                List<Integer> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    list.add(i);
                }
            });
            
            // LinkedList
            List<Integer> linkedList = new LinkedList<>();
            long linkedListTime = TimeProfiler.measureTimeWithWarmup(() -> {
                for (int i = 0; i < size; i++) {
                    linkedList.add(i);
                }
            }, 3);
            
            long linkedListMemory = MemoryProfiler.measureMemoryUsage(() -> {
                List<Integer> list = new LinkedList<>();
                for (int i = 0; i < size; i++) {
                    list.add(i);
                }
            });
            
            // Зберігаємо результати
            testResults.computeIfAbsent("addToEnd", k -> new ArrayList<>()).add(
                new TestResult("addToEnd", "ArrayList", size, arrayListTime, arrayListMemory));
            testResults.computeIfAbsent("addToEnd", k -> new ArrayList<>()).add(
                new TestResult("addToEnd", "LinkedList", size, linkedListTime, linkedListMemory));
            
            // Виводимо результати
            System.out.printf("Розмір %d:\n", size);
            System.out.printf("  ArrayList:  %d ns, %d bytes\n", arrayListTime, arrayListMemory);
            System.out.printf("  LinkedList: %d ns, %d bytes\n", linkedListTime, linkedListMemory);
            System.out.printf("  Перевага: %s (%.2fx)\n", 
                arrayListTime < linkedListTime ? "ArrayList" : "LinkedList",
                Math.max(arrayListTime, linkedListTime) / (double) Math.min(arrayListTime, linkedListTime));
        }
    }
    
    /**
     * Тест додавання елементів на початок списку
     */
    public static void testAddToBeginning() {
        System.out.println("\n=== Тест додавання на початок списку ===");
        
        int[] sizes = {SMALL_SIZE, MEDIUM_SIZE, LARGE_SIZE};
        
        for (int size : sizes) {
            // ArrayList
            long arrayListTime = TimeProfiler.measureTimeWithWarmup(() -> {
                List<Integer> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    list.add(0, i); // Додавання на початок
                }
            }, 3);
            
            long arrayListMemory = MemoryProfiler.measureMemoryUsage(() -> {
                List<Integer> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    list.add(0, i);
                }
            });
            
            // LinkedList
            long linkedListTime = TimeProfiler.measureTimeWithWarmup(() -> {
                List<Integer> list = new LinkedList<>();
                for (int i = 0; i < size; i++) {
                    list.add(0, i); // Додавання на початок
                }
            }, 3);
            
            long linkedListMemory = MemoryProfiler.measureMemoryUsage(() -> {
                List<Integer> list = new LinkedList<>();
                for (int i = 0; i < size; i++) {
                    list.add(0, i);
                }
            });
            
            // Зберігаємо результати
            testResults.computeIfAbsent("addToBeginning", k -> new ArrayList<>()).add(
                new TestResult("addToBeginning", "ArrayList", size, arrayListTime, arrayListMemory));
            testResults.computeIfAbsent("addToBeginning", k -> new ArrayList<>()).add(
                new TestResult("addToBeginning", "LinkedList", size, linkedListTime, linkedListMemory));
            
            // Виводимо результати
            System.out.printf("Розмір %d:\n", size);
            System.out.printf("  ArrayList:  %d ns, %d bytes\n", arrayListTime, arrayListMemory);
            System.out.printf("  LinkedList: %d ns, %d bytes\n", linkedListTime, linkedListMemory);
            System.out.printf("  Перевага: %s (%.2fx)\n", 
                arrayListTime < linkedListTime ? "ArrayList" : "LinkedList",
                Math.max(arrayListTime, linkedListTime) / (double) Math.min(arrayListTime, linkedListTime));
        }
    }
    
    /**
     * Тест доступу до елементів за індексом
     */
    public static void testRandomAccess() {
        System.out.println("\n=== Тест доступу до елементів за індексом ===");
        
        int[] sizes = {SMALL_SIZE, MEDIUM_SIZE, LARGE_SIZE};
        
        for (int size : sizes) {
            // Підготовка списків
            List<Integer> arrayList = new ArrayList<>();
            List<Integer> linkedList = new LinkedList<>();
            for (int i = 0; i < size; i++) {
                arrayList.add(i);
                linkedList.add(i);
            }
            
            Random random = new Random(42); // Фіксований seed для повторюваності
            
            // ArrayList
            long arrayListTime = TimeProfiler.measureTimeWithWarmup(() -> {
                for (int i = 0; i < 1000; i++) {
                    int index = random.nextInt(size);
                    arrayList.get(index);
                }
            }, 3);
            
            // LinkedList
            random.setSeed(42); // Скидаємо seed для однакових індексів
            long linkedListTime = TimeProfiler.measureTimeWithWarmup(() -> {
                for (int i = 0; i < 1000; i++) {
                    int index = random.nextInt(size);
                    linkedList.get(index);
                }
            }, 3);
            
            // Зберігаємо результати
            testResults.computeIfAbsent("randomAccess", k -> new ArrayList<>()).add(
                new TestResult("randomAccess", "ArrayList", size, arrayListTime, 0));
            testResults.computeIfAbsent("randomAccess", k -> new ArrayList<>()).add(
                new TestResult("randomAccess", "LinkedList", size, linkedListTime, 0));
            
            // Виводимо результати
            System.out.printf("Розмір %d (1000 операцій):\n", size);
            System.out.printf("  ArrayList:  %d ns\n", arrayListTime);
            System.out.printf("  LinkedList: %d ns\n", linkedListTime);
            System.out.printf("  Перевага: %s (%.2fx)\n", 
                arrayListTime < linkedListTime ? "ArrayList" : "LinkedList",
                Math.max(arrayListTime, linkedListTime) / (double) Math.min(arrayListTime, linkedListTime));
        }
    }
    
    /**
     * Тест видалення елементів з кінця
     */
    public static void testRemoveFromEnd() {
        System.out.println("\n=== Тест видалення з кінця списку ===");
        
        int[] sizes = {SMALL_SIZE, MEDIUM_SIZE, LARGE_SIZE};
        
        for (int size : sizes) {
            // ArrayList
            long arrayListTime = TimeProfiler.measureTimeWithWarmup(() -> {
                List<Integer> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    list.add(i);
                }
                for (int i = 0; i < size / 2; i++) {
                    list.remove(list.size() - 1);
                }
            }, 3);
            
            // LinkedList
            long linkedListTime = TimeProfiler.measureTimeWithWarmup(() -> {
                List<Integer> list = new LinkedList<>();
                for (int i = 0; i < size; i++) {
                    list.add(i);
                }
                for (int i = 0; i < size / 2; i++) {
                    list.remove(list.size() - 1);
                }
            }, 3);
            
            // Зберігаємо результати
            testResults.computeIfAbsent("removeFromEnd", k -> new ArrayList<>()).add(
                new TestResult("removeFromEnd", "ArrayList", size, arrayListTime, 0));
            testResults.computeIfAbsent("removeFromEnd", k -> new ArrayList<>()).add(
                new TestResult("removeFromEnd", "LinkedList", size, linkedListTime, 0));
            
            // Виводимо результати
            System.out.printf("Розмір %d (видалення половини):\n", size);
            System.out.printf("  ArrayList:  %d ns\n", arrayListTime);
            System.out.printf("  LinkedList: %d ns\n", linkedListTime);
            System.out.printf("  Перевага: %s (%.2fx)\n", 
                arrayListTime < linkedListTime ? "ArrayList" : "LinkedList",
                Math.max(arrayListTime, linkedListTime) / (double) Math.min(arrayListTime, linkedListTime));
        }
    }
    
    /**
     * Тест видалення елементів з початку
     */
    public static void testRemoveFromBeginning() {
        System.out.println("\n=== Тест видалення з початку списку ===");
        
        int[] sizes = {SMALL_SIZE, MEDIUM_SIZE, LARGE_SIZE};
        
        for (int size : sizes) {
            // ArrayList
            long arrayListTime = TimeProfiler.measureTimeWithWarmup(() -> {
                List<Integer> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    list.add(i);
                }
                for (int i = 0; i < size / 2; i++) {
                    list.remove(0); // Видалення з початку
                }
            }, 3);
            
            // LinkedList
            long linkedListTime = TimeProfiler.measureTimeWithWarmup(() -> {
                List<Integer> list = new LinkedList<>();
                for (int i = 0; i < size; i++) {
                    list.add(i);
                }
                for (int i = 0; i < size / 2; i++) {
                    list.remove(0); // Видалення з початку
                }
            }, 3);
            
            // Зберігаємо результати
            testResults.computeIfAbsent("removeFromBeginning", k -> new ArrayList<>()).add(
                new TestResult("removeFromBeginning", "ArrayList", size, arrayListTime, 0));
            testResults.computeIfAbsent("removeFromBeginning", k -> new ArrayList<>()).add(
                new TestResult("removeFromBeginning", "LinkedList", size, linkedListTime, 0));
            
            // Виводимо результати
            System.out.printf("Розмір %d (видалення половини):\n", size);
            System.out.printf("  ArrayList:  %d ns\n", arrayListTime);
            System.out.printf("  LinkedList: %d ns\n", linkedListTime);
            System.out.printf("  Перевага: %s (%.2fx)\n", 
                arrayListTime < linkedListTime ? "ArrayList" : "LinkedList",
                Math.max(arrayListTime, linkedListTime) / (double) Math.min(arrayListTime, linkedListTime));
        }
    }
    
    /**
     * Тест ітерації по списку
     */
    public static void testIteration() {
        System.out.println("\n=== Тест ітерації по списку ===");
        
        int[] sizes = {SMALL_SIZE, MEDIUM_SIZE, LARGE_SIZE};
        
        for (int size : sizes) {
            // Підготовка списків
            List<Integer> arrayList = new ArrayList<>();
            List<Integer> linkedList = new LinkedList<>();
            for (int i = 0; i < size; i++) {
                arrayList.add(i);
                linkedList.add(i);
            }
            
            // ArrayList - for-each
            long arrayListTime = TimeProfiler.measureTimeWithWarmup(() -> {
                int sum = 0;
                for (Integer value : arrayList) {
                    sum += value;
                }
            }, 3);
            
            // LinkedList - for-each
            long linkedListTime = TimeProfiler.measureTimeWithWarmup(() -> {
                int sum = 0;
                for (Integer value : linkedList) {
                    sum += value;
                }
            }, 3);
            
            // Зберігаємо результати
            testResults.computeIfAbsent("iteration", k -> new ArrayList<>()).add(
                new TestResult("iteration", "ArrayList", size, arrayListTime, 0));
            testResults.computeIfAbsent("iteration", k -> new ArrayList<>()).add(
                new TestResult("iteration", "LinkedList", size, linkedListTime, 0));
            
            // Виводимо результати
            System.out.printf("Розмір %d:\n", size);
            System.out.printf("  ArrayList:  %d ns\n", arrayListTime);
            System.out.printf("  LinkedList: %d ns\n", linkedListTime);
            System.out.printf("  Перевага: %s (%.2fx)\n", 
                arrayListTime < linkedListTime ? "ArrayList" : "LinkedList",
                Math.max(arrayListTime, linkedListTime) / (double) Math.min(arrayListTime, linkedListTime));
        }
    }
    
    /**
     * Тест вставки елементів в середину
     */
    public static void testInsertInMiddle() {
        System.out.println("\n=== Тест вставки в середину списку ===");
        
        int[] sizes = {SMALL_SIZE, MEDIUM_SIZE, LARGE_SIZE};
        
        for (int size : sizes) {
            // ArrayList
            long arrayListTime = TimeProfiler.measureTimeWithWarmup(() -> {
                List<Integer> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    list.add(i);
                }
                for (int i = 0; i < 100; i++) {
                    list.add(size / 2, i); // Вставка в середину
                }
            }, 3);
            
            // LinkedList
            long linkedListTime = TimeProfiler.measureTimeWithWarmup(() -> {
                List<Integer> list = new LinkedList<>();
                for (int i = 0; i < size; i++) {
                    list.add(i);
                }
                for (int i = 0; i < 100; i++) {
                    list.add(size / 2, i); // Вставка в середину
                }
            }, 3);
            
            // Зберігаємо результати
            testResults.computeIfAbsent("insertInMiddle", k -> new ArrayList<>()).add(
                new TestResult("insertInMiddle", "ArrayList", size, arrayListTime, 0));
            testResults.computeIfAbsent("insertInMiddle", k -> new ArrayList<>()).add(
                new TestResult("insertInMiddle", "LinkedList", size, linkedListTime, 0));
            
            // Виводимо результати
            System.out.printf("Розмір %d (100 вставок):\n", size);
            System.out.printf("  ArrayList:  %d ns\n", arrayListTime);
            System.out.printf("  LinkedList: %d ns\n", linkedListTime);
            System.out.printf("  Перевага: %s (%.2fx)\n", 
                arrayListTime < linkedListTime ? "ArrayList" : "LinkedList",
                Math.max(arrayListTime, linkedListTime) / (double) Math.min(arrayListTime, linkedListTime));
        }
    }
    
    /**
     * Створює текстові графіки порівняння
     */
    public static void createComparisonCharts() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ГРАФІКИ ПОРІВНЯННЯ ПРОДУКТИВНОСТІ");
        System.out.println("=".repeat(80));
        
        for (Map.Entry<String, List<TestResult>> entry : testResults.entrySet()) {
            String operation = entry.getKey();
            List<TestResult> results = entry.getValue();
            
            System.out.println("\n" + "-".repeat(60));
            System.out.println("ОПЕРАЦІЯ: " + operation.toUpperCase());
            System.out.println("-".repeat(60));
            
            // Групуємо результати за розміром
            Map<Integer, List<TestResult>> bySize = new HashMap<>();
            for (TestResult result : results) {
                bySize.computeIfAbsent(result.getSize(), k -> new ArrayList<>()).add(result);
            }
            
            for (Map.Entry<Integer, List<TestResult>> sizeEntry : bySize.entrySet()) {
                int size = sizeEntry.getKey();
                List<TestResult> sizeResults = sizeEntry.getValue();
                
                System.out.printf("\nРозмір: %d\n", size);
                
                // Знаходимо максимальний час для масштабування
                long maxTime = sizeResults.stream().mapToLong(TestResult::getTimeNanos).max().orElse(1);
                
                for (TestResult result : sizeResults) {
                    String listType = result.getListType();
                    long time = result.getTimeNanos();
                    
                    // Створюємо текстову діаграму
                    int barLength = (int) ((time * 50) / maxTime);
                    String bar = "█".repeat(Math.max(1, barLength));
                    
                    System.out.printf("%-10s: %s %d ns\n", listType, bar, time);
                }
            }
        }
    }
    
    /**
     * Аналізує використання пам'яті
     */
    public static void analyzeMemoryUsage() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("АНАЛІЗ ВИКОРИСТАННЯ ПАМ'ЯТІ");
        System.out.println("=".repeat(80));
        
        int[] sizes = {SMALL_SIZE, MEDIUM_SIZE, LARGE_SIZE};
        
        for (int size : sizes) {
            System.out.printf("\nРозмір списку: %d елементів\n", size);
            System.out.println("-".repeat(40));
            
            // ArrayList
            long arrayListMemory = MemoryProfiler.measureMemoryUsage(() -> {
                List<Integer> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    list.add(i);
                }
            });
            
            // LinkedList
            long linkedListMemory = MemoryProfiler.measureMemoryUsage(() -> {
                List<Integer> list = new LinkedList<>();
                for (int i = 0; i < size; i++) {
                    list.add(i);
                }
            });
            
            System.out.printf("ArrayList:  %d bytes (%.2f KB)\n", 
                arrayListMemory, arrayListMemory / 1024.0);
            System.out.printf("LinkedList: %d bytes (%.2f KB)\n", 
                linkedListMemory, linkedListMemory / 1024.0);
            
            double ratio = (double) linkedListMemory / arrayListMemory;
            System.out.printf("Співвідношення: LinkedList використовує %.2fx більше пам'яті\n", ratio);
            
            // Розрахунок на елемент
            System.out.printf("На елемент - ArrayList: %.2f bytes, LinkedList: %.2f bytes\n",
                arrayListMemory / (double) size, linkedListMemory / (double) size);
        }
    }
    
    /**
     * Рекомендації по оптимізації
     */
    public static void provideOptimizationRecommendations() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("РЕКОМЕНДАЦІЇ ПО ОПТИМІЗАЦІЇ");
        System.out.println("=".repeat(80));
        
        System.out.println("\n1. ВИБІР СТРУКТУРИ ДАНИХ:");
        System.out.println("   • ArrayList - для частого доступу за індексом та додавання в кінець");
        System.out.println("   • LinkedList - для частого додавання/видалення на початку/в середині");
        
        System.out.println("\n2. ОПТИМІЗАЦІЯ ARRAYLIST:");
        System.out.println("   • Встановлюйте початкову ємність, якщо знаєте розмір");
        System.out.println("   • Використовуйте trimToSize() після завершення додавання");
        System.out.println("   • Уникайте додавання на початок - O(n) операція");
        
        System.out.println("\n3. ОПТИМІЗАЦІЯ LINKEDLIST:");
        System.out.println("   • Використовуйте ітератори для доступу до елементів");
        System.out.println("   • Уникайте get(index) для великих індексів - O(n) операція");
        System.out.println("   • Використовуйте ListIterator для вставки/видалення");
        
        System.out.println("\n4. ЗАГАЛЬНІ РЕКОМЕНДАЦІЇ:");
        System.out.println("   • Використовуйте for-each цикл замість індексного доступу");
        System.out.println("   • Розглядайте використання ArrayDeque для черг/стеків");
        System.out.println("   • Профілюйте реальні сценарії використання");
        
        System.out.println("\n5. КРИТИЧНІ ОПЕРАЦІЇ:");
        System.out.println("   • ArrayList.get() - O(1)");
        System.out.println("   • LinkedList.get() - O(n)");
        System.out.println("   • ArrayList.add(0, element) - O(n)");
        System.out.println("   • LinkedList.add(0, element) - O(1)");
        System.out.println("   • ArrayList.add(element) - O(1) амортизовано");
        System.out.println("   • LinkedList.add(element) - O(1)");
    }
    
    /**
     * Демонстрація оптимізованих операцій
     */
    public static void demonstrateOptimizedOperations() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ДЕМОНСТРАЦІЯ ОПТИМІЗОВАНИХ ОПЕРАЦІЙ");
        System.out.println("=".repeat(80));
        
        int size = MEDIUM_SIZE;
        
        // Оптимізоване створення ArrayList
        System.out.println("\n1. Оптимізоване створення ArrayList:");
        long optimizedTime = TimeProfiler.measureTime(() -> {
            List<Integer> list = new ArrayList<>(size); // Встановлюємо початкову ємність
            for (int i = 0; i < size; i++) {
                list.add(i);
            }
        });
        
        long unoptimizedTime = TimeProfiler.measureTime(() -> {
            List<Integer> list = new ArrayList<>(); // Без початкової ємності
            for (int i = 0; i < size; i++) {
                list.add(i);
            }
        });
        
        System.out.printf("   З початковою ємністю: %d ns\n", optimizedTime);
        System.out.printf("   Без початкової ємності: %d ns\n", unoptimizedTime);
        System.out.printf("   Покращення: %.2fx\n", (double) unoptimizedTime / optimizedTime);
        
        // Оптимізована ітерація
        System.out.println("\n2. Оптимізована ітерація:");
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }
        
        // Індексна ітерація (повільна для LinkedList)
        long indexedTime = TimeProfiler.measureTime(() -> {
            int sum = 0;
            for (int i = 0; i < arrayList.size(); i++) {
                sum += arrayList.get(i);
            }
        });
        
        // For-each ітерація (швидка для обох)
        long forEachTime = TimeProfiler.measureTime(() -> {
            int sum = 0;
            for (Integer value : arrayList) {
                sum += value;
            }
        });
        
        System.out.printf("   Індексна ітерація: %d ns\n", indexedTime);
        System.out.printf("   For-each ітерація: %d ns\n", forEachTime);
        System.out.printf("   Покращення: %.2fx\n", (double) indexedTime / forEachTime);
        
        // Оптимізоване видалення з LinkedList
        System.out.println("\n3. Оптимізоване видалення з LinkedList:");
        
        // Видалення через індекс (повільне)
        List<Integer> list1 = new LinkedList<>();
        for (int i = 0; i < 1000; i++) {
            list1.add(i);
        }
        long removeByIndexTime = TimeProfiler.measureTime(() -> {
            for (int i = 0; i < 100; i++) {
                list1.remove(0);
            }
        });
        
        // Видалення через ітератор (швидке)
        List<Integer> list2 = new LinkedList<>();
        for (int i = 0; i < 1000; i++) {
            list2.add(i);
        }
        long removeByIteratorTime = TimeProfiler.measureTime(() -> {
            Iterator<Integer> iterator = list2.iterator();
            int count = 0;
            while (iterator.hasNext() && count < 100) {
                iterator.next();
                iterator.remove();
                count++;
            }
        });
        
        System.out.printf("   Видалення через індекс: %d ns\n", removeByIndexTime);
        System.out.printf("   Видалення через ітератор: %d ns\n", removeByIteratorTime);
        System.out.printf("   Покращення: %.2fx\n", (double) removeByIndexTime / removeByIteratorTime);
    }
    
    /**
     * Головний метод для запуску всіх тестів
     */
    public static void main(String[] args) {
        System.out.println("Практичне завдання №2: Оптимізація операцій з List: ArrayList vs LinkedList");
        System.out.println("=".repeat(80));
        
        try {
            // Запускаємо всі тести
            testAddToEnd();
            testAddToBeginning();
            testRandomAccess();
            testRemoveFromEnd();
            testRemoveFromBeginning();
            testIteration();
            testInsertInMiddle();
            
            // Аналізуємо результати
            createComparisonCharts();
            analyzeMemoryUsage();
            demonstrateOptimizedOperations();
            provideOptimizationRecommendations();
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("ТЕСТУВАННЯ ЗАВЕРШЕНО УСПІШНО!");
            System.out.println("=".repeat(80));
            
        } catch (Exception e) {
            System.err.println("Помилка під час тестування: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
