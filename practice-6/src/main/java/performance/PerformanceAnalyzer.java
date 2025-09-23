package performance;

import model.DataRecord;
import processor.DataProcessor;
import processor.AdvancedProcessor;
import processor.AsyncProcessor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * PerformanceAnalyzer class for measuring and comparing performance
 * of sequential vs parallel processing operations
 */
public class PerformanceAnalyzer {
    
    private final DataProcessor dataProcessor;
    private final AdvancedProcessor advancedProcessor;
    private final AsyncProcessor asyncProcessor;
    
    public PerformanceAnalyzer() {
        this.dataProcessor = new DataProcessor();
        this.advancedProcessor = new AdvancedProcessor();
        this.asyncProcessor = new AsyncProcessor();
    }

    /**
     * Compare performance of sequential and parallel processing
     * @param records List of DataRecord objects to process
     */
    public void comparePerformance(List<DataRecord> records) {
        System.out.println("=== Performance Comparison ===");
        System.out.println("Records count: " + records.size());
        System.out.println();
        
        // Test 1: Filtering operations
        System.out.println("1. Filtering Operations:");
        measureFilteringPerformance(records);
        
        // Test 2: Statistics calculation
        System.out.println("\n2. Statistics Calculation:");
        measureStatisticsPerformance(records);
        
        // Test 3: Grouping operations
        System.out.println("\n3. Grouping Operations:");
        measureGroupingPerformance(records);
        
        // Test 4: Complex aggregation
        System.out.println("\n4. Complex Aggregation:");
        measureComplexAggregationPerformance(records);
    }

    /**
     * Analyze impact of data size on performance
     * @param records List of DataRecord objects
     */
    public void analyzeSizeImpact(List<DataRecord> records) {
        System.out.println("=== Data Size Impact Analysis ===");
        
        int[] sizes = {1000, 10000, 100000, 500000, records.size()};
        
        System.out.printf("%-10s %-15s %-15s %-15s%n", 
                "Size", "Sequential(ms)", "Parallel(ms)", "Speedup");
        System.out.println("-".repeat(55));
        
        for (int size : sizes) {
            if (size > records.size()) continue;
            
            List<DataRecord> subset = records.subList(0, size);
            
            long sequentialTime = measureTime(() -> 
                dataProcessor.calculateStatistics(subset));
            
            long parallelTime = measureTime(() -> 
                dataProcessor.calculateStatisticsParallel(subset));
            
            double speedup = (double) sequentialTime / parallelTime;
            
            System.out.printf("%-10d %-15d %-15d %-15.2f%n", 
                    size, sequentialTime, parallelTime, speedup);
        }
    }

    /**
     * Measure memory usage during processing
     * @param records List of DataRecord objects
     */
    public void measureMemoryUsage(List<DataRecord> records) {
        System.out.println("=== Memory Usage Analysis ===");
        
        // Force garbage collection
        System.gc();
        long initialMemory = getUsedMemory();
        
        System.out.println("Initial memory usage: " + formatBytes(initialMemory));
        
        // Test sequential processing memory usage
        System.gc();
        long beforeSequential = getUsedMemory();
        
        dataProcessor.calculateStatistics(records);
        
        System.gc();
        long afterSequential = getUsedMemory();
        
        System.out.println("Sequential processing memory delta: " + 
                formatBytes(afterSequential - beforeSequential));
        
        // Test parallel processing memory usage
        System.gc();
        long beforeParallel = getUsedMemory();
        
        dataProcessor.calculateStatisticsParallel(records);
        
        System.gc();
        long afterParallel = getUsedMemory();
        
        System.out.println("Parallel processing memory delta: " + 
                formatBytes(afterParallel - beforeParallel));
        
        // Test async processing memory usage
        System.gc();
        long beforeAsync = getUsedMemory();
        
        CompletableFuture<Map<String, Double>> future = asyncProcessor.processAsync(records);
        try {
            future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.gc();
        long afterAsync = getUsedMemory();
        
        System.out.println("Async processing memory delta: " + 
                formatBytes(afterAsync - beforeAsync));
    }

    /**
     * Measure filtering performance
     */
    private void measureFilteringPerformance(List<DataRecord> records) {
        long sequentialTime = measureTime(() -> 
            dataProcessor.filterRecords(records, "A", 100.0));
        
        long parallelTime = measureTime(() -> 
            dataProcessor.filterRecordsParallel(records, "A", 100.0));
        
        printComparison("Filtering", sequentialTime, parallelTime);
    }

    /**
     * Measure statistics performance
     */
    private void measureStatisticsPerformance(List<DataRecord> records) {
        long sequentialTime = measureTime(() -> 
            dataProcessor.calculateStatistics(records));
        
        long parallelTime = measureTime(() -> 
            dataProcessor.calculateStatisticsParallel(records));
        
        printComparison("Statistics", sequentialTime, parallelTime);
    }

    /**
     * Measure grouping performance
     */
    private void measureGroupingPerformance(List<DataRecord> records) {
        long sequentialTime = measureTime(() -> 
            dataProcessor.groupByPriority(records));
        
        long parallelTime = measureTime(() -> 
            dataProcessor.groupByPriorityParallel(records));
        
        printComparison("Grouping", sequentialTime, parallelTime);
    }

    /**
     * Measure complex aggregation performance
     */
    private void measureComplexAggregationPerformance(List<DataRecord> records) {
        long sequentialTime = measureTime(() -> 
            advancedProcessor.aggregateByCategories(records));
        
        long parallelTime = measureTime(() -> 
            advancedProcessor.aggregateByCategories(records)); // Already parallel
        
        printComparison("Complex Aggregation", sequentialTime, parallelTime);
    }

    /**
     * Measure execution time of a runnable
     */
    private long measureTime(Runnable operation) {
        long startTime = System.currentTimeMillis();
        operation.run();
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    /**
     * Get current memory usage
     */
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Format bytes to human readable format
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    /**
     * Print performance comparison
     */
    private void printComparison(String operation, long sequentialTime, long parallelTime) {
        double speedup = (double) sequentialTime / parallelTime;
        System.out.printf("  %-20s: Sequential=%dms, Parallel=%dms, Speedup=%.2fx%n", 
                operation, sequentialTime, parallelTime, speedup);
    }

    /**
     * Comprehensive performance analysis
     */
    public void runComprehensiveAnalysis(List<DataRecord> records) {
        System.out.println("Starting comprehensive performance analysis...");
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Total memory: " + formatBytes(Runtime.getRuntime().totalMemory()));
        System.out.println("Max memory: " + formatBytes(Runtime.getRuntime().maxMemory()));
        System.out.println();
        
        comparePerformance(records);
        System.out.println();
        analyzeSizeImpact(records);
        System.out.println();
        measureMemoryUsage(records);
        
        // Cleanup
        asyncProcessor.shutdown();
    }
}
