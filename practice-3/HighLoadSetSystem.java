/**
 * Практичне завдання №3: Розробка високонавантаженої системи з використанням Set
 * 
 * Мета: Дослідити особливості реалізації та використання різних типів Set в Java,
 * оптимізувати пошук дублікатів та забезпечити ефективне управління унікальністю даних.
 * 
 * @author Student
 * @version 1.0
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Клас для роботи з високонавантаженою системою на основі Set
 */
public class HighLoadSetSystem {
    
    // Логування
    private static final Logger logger = Logger.getLogger(HighLoadSetSystem.class.getName());
    
    // Константи для тестування
    private static final int SMALL_DATASET = 10000;
    private static final int MEDIUM_DATASET = 100000;
    private static final int LARGE_DATASET = 1000000;
    private static final int VERY_LARGE_DATASET = 10000000;
    
    // Результати тестів
    private static final Map<String, PerformanceResult> performanceResults = new HashMap<>();
    
    /**
     * Клас для зберігання результатів продуктивності
     */
    static class PerformanceResult {
        private final String operation;
        private final String setType;
        private final int datasetSize;
        private final long timeNanos;
        private final long memoryUsed;
        private final int duplicatesFound;
        
        public PerformanceResult(String operation, String setType, int datasetSize, 
                               long timeNanos, long memoryUsed, int duplicatesFound) {
            this.operation = operation;
            this.setType = setType;
            this.datasetSize = datasetSize;
            this.timeNanos = timeNanos;
            this.memoryUsed = memoryUsed;
            this.duplicatesFound = duplicatesFound;
        }
        
        // Геттери
        public String getOperation() { return operation; }
        public String getSetType() { return setType; }
        public int getDatasetSize() { return datasetSize; }
        public long getTimeNanos() { return timeNanos; }
        public long getMemoryUsed() { return memoryUsed; }
        public int getDuplicatesFound() { return duplicatesFound; }
        
        @Override
        public String toString() {
            return String.format("%s %s (size=%d): %d ns, %d bytes, %d duplicates", 
                operation, setType, datasetSize, timeNanos, memoryUsed, duplicatesFound);
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
     * Генератор тестових даних
     */
    static class DataGenerator {
        private static final Random random = new Random(42);
        
        /**
         * Генерує набір даних з певним відсотком дублікатів
         */
        public static List<String> generateDataset(int size, double duplicatePercentage) {
            List<String> dataset = new ArrayList<>();
            int uniqueCount = (int) (size * (1 - duplicatePercentage));
            int duplicateCount = size - uniqueCount;
            
            // Генеруємо унікальні елементи
            for (int i = 0; i < uniqueCount; i++) {
                dataset.add("item_" + i);
            }
            
            // Додаємо дублікати
            for (int i = 0; i < duplicateCount; i++) {
                int randomIndex = random.nextInt(uniqueCount);
                dataset.add("item_" + randomIndex);
            }
            
            // Перемішуємо
            Collections.shuffle(dataset, random);
            return dataset;
        }
        
        /**
         * Генерує набір числових даних
         */
        public static List<Integer> generateNumericDataset(int size, double duplicatePercentage) {
            List<Integer> dataset = new ArrayList<>();
            int uniqueCount = (int) (size * (1 - duplicatePercentage));
            int duplicateCount = size - uniqueCount;
            
            // Генеруємо унікальні елементи
            for (int i = 0; i < uniqueCount; i++) {
                dataset.add(i);
            }
            
            // Додаємо дублікати
            for (int i = 0; i < duplicateCount; i++) {
                int randomIndex = random.nextInt(uniqueCount);
                dataset.add(randomIndex);
            }
            
            // Перемішуємо
            Collections.shuffle(dataset, random);
            return dataset;
        }
    }
    
    /**
     * Клас для пошуку дублікатів
     */
    static class DuplicateFinder {
        
        /**
         * Знаходить дублікати використовуючи HashSet
         */
        public static Set<String> findDuplicatesWithHashSet(List<String> dataset) {
            Set<String> seen = new HashSet<>();
            Set<String> duplicates = new HashSet<>();
            
            for (String item : dataset) {
                if (!seen.add(item)) {
                    duplicates.add(item);
                }
            }
            
            return duplicates;
        }
        
        /**
         * Знаходить дублікати використовуючи TreeSet
         */
        public static Set<String> findDuplicatesWithTreeSet(List<String> dataset) {
            Set<String> seen = new TreeSet<>();
            Set<String> duplicates = new TreeSet<>();
            
            for (String item : dataset) {
                if (!seen.add(item)) {
                    duplicates.add(item);
                }
            }
            
            return duplicates;
        }
        
        /**
         * Знаходить дублікати використовуючи LinkedHashSet
         */
        public static Set<String> findDuplicatesWithLinkedHashSet(List<String> dataset) {
            Set<String> seen = new LinkedHashSet<>();
            Set<String> duplicates = new LinkedHashSet<>();
            
            for (String item : dataset) {
                if (!seen.add(item)) {
                    duplicates.add(item);
                }
            }
            
            return duplicates;
        }
        
        /**
         * Знаходить дублікати використовуючи Stream API
         */
        public static Set<String> findDuplicatesWithStream(List<String> dataset) {
            return dataset.stream()
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        }
    }
    
    /**
     * Клас для паралельного пошуку дублікатів
     */
    static class ParallelDuplicateFinder {
        
        /**
         * Паралельний пошук дублікатів з використанням ForkJoinPool
         */
        public static Set<String> findDuplicatesParallel(List<String> dataset, int threadCount) {
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            int chunkSize = dataset.size() / threadCount;
            List<Future<Map<String, Integer>>> futures = new ArrayList<>();
            
            try {
                // Розділяємо дані на частини
                for (int i = 0; i < threadCount; i++) {
                    int start = i * chunkSize;
                    int end = (i == threadCount - 1) ? dataset.size() : (i + 1) * chunkSize;
                    List<String> chunk = dataset.subList(start, end);
                    
                    Future<Map<String, Integer>> future = executor.submit(() -> {
                        Map<String, Integer> counts = new HashMap<>();
                        for (String item : chunk) {
                            counts.merge(item, 1, Integer::sum);
                        }
                        return counts;
                    });
                    futures.add(future);
                }
                
                // Об'єднуємо результати
                Map<String, Integer> totalCounts = new HashMap<>();
                for (Future<Map<String, Integer>> future : futures) {
                    Map<String, Integer> chunkCounts = future.get();
                    for (Map.Entry<String, Integer> entry : chunkCounts.entrySet()) {
                        totalCounts.merge(entry.getKey(), entry.getValue(), Integer::sum);
                    }
                }
                
                // Знаходимо дублікати
                return totalCounts.entrySet().stream()
                    .filter(entry -> entry.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
                    
            } catch (InterruptedException | ExecutionException e) {
                logger.severe("Помилка під час паралельного пошуку: " + e.getMessage());
                return new HashSet<>();
            } finally {
                executor.shutdown();
            }
        }
        
        /**
         * Паралельний пошук з використанням CompletableFuture
         */
        public static CompletableFuture<Set<String>> findDuplicatesAsync(List<String> dataset) {
            return CompletableFuture.supplyAsync(() -> {
                return DuplicateFinder.findDuplicatesWithHashSet(dataset);
            });
        }
    }
    
    /**
     * Клас для оптимізації пам'яті
     */
    static class MemoryOptimizer {
        
        /**
         * Оптимізований пошук дублікатів з мінімальним використанням пам'яті
         */
        public static Set<String> findDuplicatesMemoryOptimized(List<String> dataset) {
            // Використовуємо ConcurrentHashMap для thread-safety та ефективності
            ConcurrentHashMap<String, AtomicLong> counts = new ConcurrentHashMap<>();
            
            // Паралельний підрахунок
            dataset.parallelStream().forEach(item -> 
                counts.computeIfAbsent(item, k -> new AtomicLong()).incrementAndGet()
            );
            
            // Знаходимо дублікати
            return counts.entrySet().stream()
                .filter(entry -> entry.getValue().get() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        }
        
        /**
         * Пошук дублікатів з обмеженням пам'яті
         */
        public static Set<String> findDuplicatesWithMemoryLimit(List<String> dataset, int maxMemoryMB) {
            long maxMemoryBytes = maxMemoryMB * 1024L * 1024L;
            Set<String> seen = new HashSet<>();
            Set<String> duplicates = new HashSet<>();
            
            for (String item : dataset) {
                // Перевіряємо використання пам'яті
                if (MemoryProfiler.getUsedMemory() > maxMemoryBytes) {
                    logger.warning("Досягнуто ліміт пам'яті, очищуємо кеш");
                    seen.clear();
                    System.gc();
                }
                
                if (!seen.add(item)) {
                    duplicates.add(item);
                }
            }
            
            return duplicates;
        }
    }
    
    /**
     * Тест базових операцій з HashSet та TreeSet
     */
    public static void testBasicOperations() {
        logger.info("=== Тест базових операцій з HashSet та TreeSet ===");
        
        int[] sizes = {SMALL_DATASET, MEDIUM_DATASET, LARGE_DATASET};
        
        for (int size : sizes) {
            List<String> dataset = DataGenerator.generateDataset(size, 0.1); // 10% дублікатів
            
            // HashSet операції
            long hashSetTime = TimeProfiler.measureTimeWithWarmup(() -> {
                Set<String> set = new HashSet<>();
                for (String item : dataset) {
                    set.add(item);
                }
                for (String item : dataset) {
                    set.contains(item);
                }
                for (String item : dataset.subList(0, Math.min(1000, dataset.size()))) {
                    set.remove(item);
                }
            }, 3);
            
            long hashSetMemory = MemoryProfiler.measureMemoryUsage(() -> {
                Set<String> set = new HashSet<>();
                for (String item : dataset) {
                    set.add(item);
                }
            });
            
            // TreeSet операції
            long treeSetTime = TimeProfiler.measureTimeWithWarmup(() -> {
                Set<String> set = new TreeSet<>();
                for (String item : dataset) {
                    set.add(item);
                }
                for (String item : dataset) {
                    set.contains(item);
                }
                for (String item : dataset.subList(0, Math.min(1000, dataset.size()))) {
                    set.remove(item);
                }
            }, 3);
            
            long treeSetMemory = MemoryProfiler.measureMemoryUsage(() -> {
                Set<String> set = new TreeSet<>();
                for (String item : dataset) {
                    set.add(item);
                }
            });
            
            // Зберігаємо результати
            performanceResults.put("basic_operations_hashset_" + size, 
                new PerformanceResult("basic_operations", "HashSet", size, hashSetTime, hashSetMemory, 0));
            performanceResults.put("basic_operations_treeset_" + size, 
                new PerformanceResult("basic_operations", "TreeSet", size, treeSetTime, treeSetMemory, 0));
            
            logger.info(String.format("Розмір %d: HashSet %d ns, %d bytes; TreeSet %d ns, %d bytes", 
                size, hashSetTime, hashSetMemory, treeSetTime, treeSetMemory));
        }
    }
    
    /**
     * Тест пошуку дублікатів
     */
    public static void testDuplicateFinding() {
        logger.info("=== Тест пошуку дублікатів ===");
        
        int[] sizes = {SMALL_DATASET, MEDIUM_DATASET, LARGE_DATASET};
        double[] duplicatePercentages = {0.1, 0.3, 0.5}; // 10%, 30%, 50% дублікатів
        
        for (int size : sizes) {
            for (double dupPercent : duplicatePercentages) {
                List<String> dataset = DataGenerator.generateDataset(size, dupPercent);
                
                // HashSet
                long hashSetTime = TimeProfiler.measureTimeWithWarmup(() -> {
                    Set<String> duplicates = DuplicateFinder.findDuplicatesWithHashSet(dataset);
                }, 3);
                
                Set<String> hashSetDuplicates = DuplicateFinder.findDuplicatesWithHashSet(dataset);
                long hashSetMemory = MemoryProfiler.measureMemoryUsage(() -> {
                    DuplicateFinder.findDuplicatesWithHashSet(dataset);
                });
                
                // TreeSet
                long treeSetTime = TimeProfiler.measureTimeWithWarmup(() -> {
                    Set<String> duplicates = DuplicateFinder.findDuplicatesWithTreeSet(dataset);
                }, 3);
                
                Set<String> treeSetDuplicates = DuplicateFinder.findDuplicatesWithTreeSet(dataset);
                long treeSetMemory = MemoryProfiler.measureMemoryUsage(() -> {
                    DuplicateFinder.findDuplicatesWithTreeSet(dataset);
                });
                
                // Stream API
                long streamTime = TimeProfiler.measureTimeWithWarmup(() -> {
                    Set<String> duplicates = DuplicateFinder.findDuplicatesWithStream(dataset);
                }, 3);
                
                Set<String> streamDuplicates = DuplicateFinder.findDuplicatesWithStream(dataset);
                
                // Зберігаємо результати
                String key = String.format("duplicates_%d_%.1f", size, dupPercent);
                performanceResults.put(key + "_hashset", 
                    new PerformanceResult("duplicates", "HashSet", size, hashSetTime, hashSetMemory, hashSetDuplicates.size()));
                performanceResults.put(key + "_treeset", 
                    new PerformanceResult("duplicates", "TreeSet", size, treeSetTime, treeSetMemory, treeSetDuplicates.size()));
                performanceResults.put(key + "_stream", 
                    new PerformanceResult("duplicates", "Stream", size, streamTime, 0, streamDuplicates.size()));
                
                logger.info(String.format("Розмір %d, дублікатів %.1f%%: HashSet %d ns, TreeSet %d ns, Stream %d ns", 
                    size, dupPercent * 100, hashSetTime, treeSetTime, streamTime));
            }
        }
    }
    
    /**
     * Тест паралельного пошуку дублікатів
     */
    public static void testParallelDuplicateFinding() {
        logger.info("=== Тест паралельного пошуку дублікатів ===");
        
        int[] sizes = {MEDIUM_DATASET, LARGE_DATASET, VERY_LARGE_DATASET};
        int[] threadCounts = {2, 4, 8};
        
        for (int size : sizes) {
            List<String> dataset = DataGenerator.generateDataset(size, 0.2); // 20% дублікатів
            
            // Послідовний пошук
            long sequentialTime = TimeProfiler.measureTimeWithWarmup(() -> {
                DuplicateFinder.findDuplicatesWithHashSet(dataset);
            }, 3);
            
            for (int threads : threadCounts) {
                long parallelTime = TimeProfiler.measureTimeWithWarmup(() -> {
                    ParallelDuplicateFinder.findDuplicatesParallel(dataset, threads);
                }, 3);
                
                Set<String> parallelDuplicates = ParallelDuplicateFinder.findDuplicatesParallel(dataset, threads);
                
                // Зберігаємо результати
                String key = String.format("parallel_%d_%d", size, threads);
                performanceResults.put(key, 
                    new PerformanceResult("parallel", "Parallel_" + threads, size, parallelTime, 0, parallelDuplicates.size()));
                
                double speedup = (double) sequentialTime / parallelTime;
                logger.info(String.format("Розмір %d, %d потоків: %d ns (speedup: %.2fx)", 
                    size, threads, parallelTime, speedup));
            }
        }
    }
    
    /**
     * Тест оптимізації пам'яті
     */
    public static void testMemoryOptimization() {
        logger.info("=== Тест оптимізації пам'яті ===");
        
        int[] sizes = {MEDIUM_DATASET, LARGE_DATASET};
        
        for (int size : sizes) {
            List<String> dataset = DataGenerator.generateDataset(size, 0.3); // 30% дублікатів
            
            // Стандартний пошук
            long standardTime = TimeProfiler.measureTimeWithWarmup(() -> {
                DuplicateFinder.findDuplicatesWithHashSet(dataset);
            }, 3);
            
            long standardMemory = MemoryProfiler.measureMemoryUsage(() -> {
                DuplicateFinder.findDuplicatesWithHashSet(dataset);
            });
            
            // Оптимізований пошук
            long optimizedTime = TimeProfiler.measureTimeWithWarmup(() -> {
                MemoryOptimizer.findDuplicatesMemoryOptimized(dataset);
            }, 3);
            
            long optimizedMemory = MemoryProfiler.measureMemoryUsage(() -> {
                MemoryOptimizer.findDuplicatesMemoryOptimized(dataset);
            });
            
            // Пошук з обмеженням пам'яті
            long limitedTime = TimeProfiler.measureTimeWithWarmup(() -> {
                MemoryOptimizer.findDuplicatesWithMemoryLimit(dataset, 100); // 100MB ліміт
            }, 3);
            
            Set<String> standardDuplicates = DuplicateFinder.findDuplicatesWithHashSet(dataset);
            Set<String> optimizedDuplicates = MemoryOptimizer.findDuplicatesMemoryOptimized(dataset);
            Set<String> limitedDuplicates = MemoryOptimizer.findDuplicatesWithMemoryLimit(dataset, 100);
            
            // Зберігаємо результати
            performanceResults.put("memory_standard_" + size, 
                new PerformanceResult("memory", "Standard", size, standardTime, standardMemory, standardDuplicates.size()));
            performanceResults.put("memory_optimized_" + size, 
                new PerformanceResult("memory", "Optimized", size, optimizedTime, optimizedMemory, optimizedDuplicates.size()));
            performanceResults.put("memory_limited_" + size, 
                new PerformanceResult("memory", "Limited", size, limitedTime, 0, limitedDuplicates.size()));
            
            logger.info(String.format("Розмір %d: Standard %d ns, %d bytes; Optimized %d ns, %d bytes; Limited %d ns", 
                size, standardTime, standardMemory, optimizedTime, optimizedMemory, limitedTime));
        }
    }
    
    /**
     * Створює звіт про продуктивність
     */
    public static void generatePerformanceReport() {
        logger.info("=== ЗВІТ ПРО ПРОДУКТИВНІСТЬ ===");
        
        // Групуємо результати за операціями
        Map<String, List<PerformanceResult>> byOperation = performanceResults.values().stream()
            .collect(Collectors.groupingBy(PerformanceResult::getOperation));
        
        for (Map.Entry<String, List<PerformanceResult>> entry : byOperation.entrySet()) {
            String operation = entry.getKey();
            List<PerformanceResult> results = entry.getValue();
            
            logger.info(String.format("\n--- %s ---", operation.toUpperCase()));
            
            // Групуємо за розміром даних
            Map<Integer, List<PerformanceResult>> bySize = results.stream()
                .collect(Collectors.groupingBy(PerformanceResult::getDatasetSize));
            
            for (Map.Entry<Integer, List<PerformanceResult>> sizeEntry : bySize.entrySet()) {
                int size = sizeEntry.getKey();
                List<PerformanceResult> sizeResults = sizeEntry.getValue();
                
                logger.info(String.format("Розмір даних: %d", size));
                
                // Сортуємо за часом виконання
                sizeResults.sort(Comparator.comparing(PerformanceResult::getTimeNanos));
                
                for (PerformanceResult result : sizeResults) {
                    logger.info(String.format("  %s: %d ns, %d bytes, %d дублікатів", 
                        result.getSetType(), result.getTimeNanos(), result.getMemoryUsed(), result.getDuplicatesFound()));
                }
            }
        }
    }
    
    /**
     * Демонстрація роботи з різними типами Set
     */
    public static void demonstrateSetTypes() {
        logger.info("=== ДЕМОНСТРАЦІЯ РІЗНИХ ТИПІВ SET ===");
        
        List<String> data = Arrays.asList("apple", "banana", "cherry", "apple", "date", "banana", "elderberry");
        
        // HashSet - неупорядкований
        Set<String> hashSet = new HashSet<>(data);
        logger.info("HashSet: " + hashSet);
        
        // TreeSet - відсортований
        Set<String> treeSet = new TreeSet<>(data);
        logger.info("TreeSet: " + treeSet);
        
        // LinkedHashSet - зберігає порядок вставки
        Set<String> linkedHashSet = new LinkedHashSet<>(data);
        logger.info("LinkedHashSet: " + linkedHashSet);
        
        // Демонстрація операцій
        logger.info("HashSet містить 'apple': " + hashSet.contains("apple"));
        logger.info("TreeSet розмір: " + treeSet.size());
        logger.info("LinkedHashSet перший елемент: " + linkedHashSet.iterator().next());
    }
    
    /**
     * Демонстрація високонавантаженої системи
     */
    public static void demonstrateHighLoadSystem() {
        logger.info("=== ДЕМОНСТРАЦІЯ ВИСОКОНАВАНТАЖЕНОЇ СИСТЕМИ ===");
        
        // Симуляція високого навантаження
        int datasetSize = LARGE_DATASET;
        List<String> dataset = DataGenerator.generateDataset(datasetSize, 0.4); // 40% дублікатів
        
        logger.info(String.format("Обробка %d елементів з %.1f%% дублікатів", 
            datasetSize, 0.4 * 100));
        
        // Паралельна обробка
        long startTime = System.nanoTime();
        
        CompletableFuture<Set<String>> future1 = ParallelDuplicateFinder.findDuplicatesAsync(dataset);
        CompletableFuture<Set<String>> future2 = CompletableFuture.supplyAsync(() -> 
            DuplicateFinder.findDuplicatesWithHashSet(dataset));
        CompletableFuture<Set<String>> future3 = CompletableFuture.supplyAsync(() -> 
            MemoryOptimizer.findDuplicatesMemoryOptimized(dataset));
        
        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(future1, future2, future3);
            allFutures.get(30, TimeUnit.SECONDS); // Таймаут 30 секунд
            
            Set<String> result1 = future1.get();
            Set<String> result2 = future2.get();
            Set<String> result3 = future3.get();
            
            long endTime = System.nanoTime();
            long totalTime = endTime - startTime;
            
            logger.info(String.format("Паралельна обробка завершена за %d ns", totalTime));
            logger.info(String.format("Знайдено дублікатів: %d, %d, %d", 
                result1.size(), result2.size(), result3.size()));
            
        } catch (Exception e) {
            logger.severe("Помилка під час паралельної обробки: " + e.getMessage());
        }
    }
    
    /**
     * Налаштування логування
     */
    private static void setupLogging() {
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }
        
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("[%s] %s: %s%n",
                    record.getLevel(),
                    record.getLoggerName(),
                    record.getMessage());
            }
        });
        
        rootLogger.addHandler(consoleHandler);
        rootLogger.setLevel(Level.INFO);
    }
    
    /**
     * Головний метод
     */
    public static void main(String[] args) {
        // Налаштування логування
        setupLogging();
        
        logger.info("Практичне завдання №3: Розробка високонавантаженої системи з використанням Set");
        logger.info("=".repeat(80));
        
        try {
            // Демонстрація різних типів Set
            demonstrateSetTypes();
            
            // Тести продуктивності
            testBasicOperations();
            testDuplicateFinding();
            testParallelDuplicateFinding();
            testMemoryOptimization();
            
            // Генерація звіту
            generatePerformanceReport();
            
            // Демонстрація високонавантаженої системи
            demonstrateHighLoadSystem();
            
            logger.info("=".repeat(80));
            logger.info("ТЕСТУВАННЯ ЗАВЕРШЕНО УСПІШНО!");
            logger.info("=".repeat(80));
            
        } catch (Exception e) {
            logger.severe("Помилка під час тестування: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
