package processor;

import model.DataRecord;
import util.DataGenerator;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.DoubleSummaryStatistics;

/**
 * Test class for processor functionality
 */
public class ProcessorTests {
    
    private final DataGenerator dataGenerator;
    private final DataProcessor dataProcessor;
    private final AdvancedProcessor advancedProcessor;
    
    public ProcessorTests() {
        this.dataGenerator = new DataGenerator();
        this.dataProcessor = new DataProcessor();
        this.advancedProcessor = new AdvancedProcessor();
    }
    
    /**
     * Run all processor tests
     */
    public void runAllTests() {
        System.out.println("=== Running Processor Tests ===");
        
        // Generate test data
        List<DataRecord> testData = generateTestData();
        System.out.println("Generated " + testData.size() + " test records");
        
        // Test DataProcessor
        testDataProcessor(testData);
        
        // Test AdvancedProcessor
        testAdvancedProcessor(testData);
        
        System.out.println("All processor tests completed successfully!");
    }
    
    /**
     * Generate test data for testing
     */
    private List<DataRecord> generateTestData() {
        return Arrays.asList(
            new DataRecord(1, "A", 100.0, LocalDateTime.now(), 1, "ACTIVE", Arrays.asList("urgent")),
            new DataRecord(2, "B", 200.0, LocalDateTime.now(), 2, "PENDING", Arrays.asList("normal")),
            new DataRecord(3, "A", 150.0, LocalDateTime.now(), 3, "ACTIVE", Arrays.asList("high")),
            new DataRecord(4, "C", 300.0, LocalDateTime.now(), 4, "COMPLETED", Arrays.asList("critical")),
            new DataRecord(5, "B", 250.0, LocalDateTime.now(), 5, "ACTIVE", Arrays.asList("urgent", "high"))
        );
    }
    
    /**
     * Test DataProcessor functionality
     */
    private void testDataProcessor(List<DataRecord> testData) {
        System.out.println("\n--- Testing DataProcessor ---");
        
        // Test filtering
        List<DataRecord> filtered = dataProcessor.filterRecords(testData, "A", 100.0);
        System.out.println("Filtered records (category A, value >= 100): " + filtered.size());
        
        // Test statistics
        Map<String, DoubleSummaryStatistics> stats = dataProcessor.calculateStatistics(testData);
        System.out.println("Statistics by category:");
        stats.forEach((category, stat) -> 
            System.out.println("  " + category + ": count=" + stat.getCount() + 
                             ", avg=" + String.format("%.2f", stat.getAverage())));
        
        // Test grouping
        Map<Integer, List<DataRecord>> grouped = dataProcessor.groupByPriority(testData);
        System.out.println("Grouped by priority:");
        grouped.forEach((priority, records) -> 
            System.out.println("  Priority " + priority + ": " + records.size() + " records"));
        
        // Test parallel vs sequential
        long startTime = System.currentTimeMillis();
        dataProcessor.filterRecords(testData, "A", 100.0);
        long sequentialTime = System.currentTimeMillis() - startTime;
        
        startTime = System.currentTimeMillis();
        dataProcessor.filterRecordsParallel(testData, "A", 100.0);
        long parallelTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Performance comparison:");
        System.out.println("  Sequential: " + sequentialTime + "ms");
        System.out.println("  Parallel: " + parallelTime + "ms");
    }
    
    /**
     * Test AdvancedProcessor functionality
     */
    private void testAdvancedProcessor(List<DataRecord> testData) {
        System.out.println("\n--- Testing AdvancedProcessor ---");
        
        // Test complex aggregation
        Map<String, Map<Integer, Double>> aggregated = advancedProcessor.aggregateByCategories(testData);
        System.out.println("Complex aggregation by category and priority:");
        aggregated.forEach((category, priorityMap) -> {
            System.out.println("  Category " + category + ":");
            priorityMap.forEach((priority, avgValue) -> 
                System.out.println("    Priority " + priority + ": " + String.format("%.2f", avgValue)));
        });
        
        // Test top-N by category
        Map<String, List<DataRecord>> topN = advancedProcessor.findTopNByCategory(testData, 2);
        System.out.println("Top 2 records by category:");
        topN.forEach((category, records) -> {
            System.out.println("  Category " + category + ":");
            records.forEach(record -> 
                System.out.println("    ID=" + record.getId() + ", Value=" + record.getValue()));
        });
        
        // Test time interval analysis
        Map<java.time.LocalDate, DoubleSummaryStatistics> timeAnalysis = 
            advancedProcessor.analyzeByTimeIntervals(testData);
        System.out.println("Time interval analysis:");
        timeAnalysis.forEach((date, stat) -> 
            System.out.println("  " + date + ": count=" + stat.getCount() + 
                             ", avg=" + String.format("%.2f", stat.getAverage())));
        
        // Test weighted average
        double weightedAvg = advancedProcessor.calculateWeightedAverage(testData);
        System.out.println("Weighted average by priority: " + String.format("%.2f", weightedAvg));
        
        // Test correlation
        double correlation = advancedProcessor.calculatePriorityValueCorrelation(testData);
        System.out.println("Priority-Value correlation: " + String.format("%.4f", correlation));
    }
    
    /**
     * Test with larger dataset
     */
    public void testWithLargeDataset() {
        System.out.println("\n=== Testing with Large Dataset ===");
        
        // Generate larger dataset
        List<DataRecord> largeData = dataGenerator.generateData(10000);
        System.out.println("Generated " + largeData.size() + " records for large dataset test");
        
        // Test performance with large dataset
        long startTime = System.currentTimeMillis();
        Map<String, DoubleSummaryStatistics> stats = dataProcessor.calculateStatistics(largeData);
        long sequentialTime = System.currentTimeMillis() - startTime;
        
        startTime = System.currentTimeMillis();
        Map<String, DoubleSummaryStatistics> parallelStats = dataProcessor.calculateStatisticsParallel(largeData);
        long parallelTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Large dataset performance:");
        System.out.println("  Sequential: " + sequentialTime + "ms");
        System.out.println("  Parallel: " + parallelTime + "ms");
        System.out.println("  Speedup: " + String.format("%.2fx", (double) sequentialTime / parallelTime));
        
        // Verify results are the same
        boolean resultsMatch = stats.equals(parallelStats);
        System.out.println("  Results match: " + resultsMatch);
    }
    
    /**
     * Main method for running tests
     */
    public static void main(String[] args) {
        ProcessorTests tests = new ProcessorTests();
        tests.runAllTests();
        tests.testWithLargeDataset();
    }
}
