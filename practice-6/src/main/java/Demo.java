import model.DataRecord;
import util.DataGenerator;
import processor.DataProcessor;
import processor.AdvancedProcessor;
import processor.AsyncProcessor;
import performance.PerformanceAnalyzer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Demo class for Parallel Processing with Stream API
 * Demonstrates all implemented functionality
 */
public class Demo {
    
    public static void main(String[] args) {
        System.out.println("=== Enterprise Java Programming - Practice 6 ===");
        System.out.println("Parallel Processing of Collections with Stream API");
        System.out.println("==================================================");
        
        Demo demo = new Demo();
        demo.runDemo();
    }
    
    /**
     * Run the complete demonstration
     */
    public void runDemo() {
        try {
            // Initialize components
            DataGenerator dataGenerator = new DataGenerator();
            DataProcessor dataProcessor = new DataProcessor();
            AdvancedProcessor advancedProcessor = new AdvancedProcessor();
            AsyncProcessor asyncProcessor = new AsyncProcessor();
            PerformanceAnalyzer performanceAnalyzer = new PerformanceAnalyzer();
            
            // Generate test data
            System.out.println("Generating test data...");
            List<DataRecord> testData = dataGenerator.generateData(100000);
            System.out.println("Generated " + testData.size() + " records");
            System.out.println();
            
            // Part 1: Basic DataRecord demonstration
            demonstrateDataRecord(testData);
            
            // Part 2: Basic parallel processing
            demonstrateBasicProcessing(dataProcessor, testData);
            
            // Part 3: Complex aggregation
            demonstrateComplexAggregation(advancedProcessor, testData);
            
            // Part 4: Asynchronous processing
            demonstrateAsyncProcessing(asyncProcessor, testData);
            
            // Part 5: Performance measurement
            demonstratePerformanceAnalysis(performanceAnalyzer, testData);
            
            // Cleanup
            asyncProcessor.shutdown();
            
            System.out.println("\n=== Demo completed successfully! ===");
            
        } catch (Exception e) {
            System.err.println("Error during demo execution: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Demonstrate DataRecord functionality
     */
    private void demonstrateDataRecord(List<DataRecord> testData) {
        System.out.println("=== Part 1: DataRecord Demonstration ===");
        
        // Show sample records
        System.out.println("Sample records:");
        testData.stream().limit(3).forEach(System.out::println);
        
        // Show statistics
        long totalRecords = testData.size();
        long uniqueCategories = testData.stream().map(DataRecord::getCategory).distinct().count();
        double avgValue = testData.stream().mapToDouble(DataRecord::getValue).average().orElse(0.0);
        
        System.out.println("\nData statistics:");
        System.out.println("Total records: " + totalRecords);
        System.out.println("Unique categories: " + uniqueCategories);
        System.out.println("Average value: " + String.format("%.2f", avgValue));
        System.out.println();
    }
    
    /**
     * Demonstrate basic processing operations
     */
    private void demonstrateBasicProcessing(DataProcessor dataProcessor, List<DataRecord> testData) {
        System.out.println("=== Part 2: Basic Parallel Processing ===");
        
        // Filtering demonstration
        System.out.println("Filtering records (category A, value >= 1000):");
        List<DataRecord> filtered = dataProcessor.filterRecordsParallel(testData, "A", 1000.0);
        System.out.println("Found " + filtered.size() + " records");
        
        // Statistics demonstration
        System.out.println("\nStatistics by category:");
        var stats = dataProcessor.calculateStatisticsParallel(testData);
        stats.forEach((category, stat) -> 
            System.out.println("  " + category + ": count=" + stat.getCount() + 
                             ", avg=" + String.format("%.2f", stat.getAverage())));
        
        // Grouping demonstration
        System.out.println("\nGrouping by priority:");
        var grouped = dataProcessor.groupByPriorityParallel(testData);
        grouped.forEach((priority, records) -> 
            System.out.println("  Priority " + priority + ": " + records.size() + " records"));
        
        System.out.println();
    }
    
    /**
     * Demonstrate complex aggregation
     */
    private void demonstrateComplexAggregation(AdvancedProcessor advancedProcessor, List<DataRecord> testData) {
        System.out.println("=== Part 3: Complex Aggregation ===");
        
        // Complex aggregation by category and priority
        System.out.println("Aggregation by category and priority:");
        var aggregated = advancedProcessor.aggregateByCategories(testData);
        aggregated.forEach((category, priorityMap) -> {
            System.out.println("  Category " + category + ":");
            priorityMap.forEach((priority, avgValue) -> 
                System.out.println("    Priority " + priority + ": " + String.format("%.2f", avgValue)));
        });
        
        // Top-N by category
        System.out.println("\nTop 3 records by category:");
        var topN = advancedProcessor.findTopNByCategory(testData, 3);
        topN.forEach((category, records) -> {
            System.out.println("  Category " + category + ":");
            records.forEach(record -> 
                System.out.println("    ID=" + record.getId() + ", Value=" + String.format("%.2f", record.getValue())));
        });
        
        // Time interval analysis
        System.out.println("\nTime interval analysis (last 5 days):");
        var timeAnalysis = advancedProcessor.analyzeByTimeIntervals(testData);
        timeAnalysis.entrySet().stream()
            .sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey()))
            .limit(5)
            .forEach(entry -> 
                System.out.println("  " + entry.getKey() + ": count=" + entry.getValue().getCount() + 
                                 ", avg=" + String.format("%.2f", entry.getValue().getAverage())));
        
        System.out.println();
    }
    
    /**
     * Demonstrate asynchronous processing
     */
    private void demonstrateAsyncProcessing(AsyncProcessor asyncProcessor, List<DataRecord> testData) {
        System.out.println("=== Part 4: Asynchronous Processing ===");
        
        try {
            // Basic async processing
            System.out.println("Running async processing...");
            CompletableFuture<Map<String, Double>> future = asyncProcessor.processAsync(testData);
            Map<String, Double> result = future.get();
            System.out.println("Async processing completed. Categories processed: " + result.size());
            
            // Multiple operations
            System.out.println("Running multiple async operations...");
            CompletableFuture<Map<String, Object>> multiOpResult = asyncProcessor.processMultipleOperationsAsync(testData);
            Map<String, Object> multiOpResults = multiOpResult.get();
            System.out.println("Multiple operations completed. Results: " + multiOpResults.keySet());
            
        } catch (Exception e) {
            System.err.println("Error in async processing: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * Demonstrate performance analysis
     */
    private void demonstratePerformanceAnalysis(PerformanceAnalyzer performanceAnalyzer, List<DataRecord> testData) {
        System.out.println("=== Part 5: Performance Analysis ===");
        
        // Run comprehensive performance analysis
        performanceAnalyzer.runComprehensiveAnalysis(testData);
        
        System.out.println();
    }
}
