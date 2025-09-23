package performance;

import model.DataRecord;
import util.DataGenerator;
import processor.AsyncProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Test class for performance analysis functionality
 */
public class PerformanceTests {
    
    private final DataGenerator dataGenerator;
    private final PerformanceAnalyzer performanceAnalyzer;
    private final AsyncProcessor asyncProcessor;
    
    public PerformanceTests() {
        this.dataGenerator = new DataGenerator();
        this.performanceAnalyzer = new PerformanceAnalyzer();
        this.asyncProcessor = new AsyncProcessor();
    }
    
    /**
     * Run all performance tests
     */
    public void runAllTests() {
        System.out.println("=== Running Performance Tests ===");
        
        // Generate test data
        List<DataRecord> testData = dataGenerator.generateData(100000);
        System.out.println("Generated " + testData.size() + " test records");
        
        // Test performance comparison
        testPerformanceComparison(testData);
        
        // Test size impact analysis
        testSizeImpactAnalysis(testData);
        
        // Test memory usage
        testMemoryUsage(testData);
        
        // Test async processing
        testAsyncProcessing(testData);
        
        System.out.println("All performance tests completed successfully!");
    }
    
    /**
     * Test performance comparison functionality
     */
    private void testPerformanceComparison(List<DataRecord> testData) {
        System.out.println("\n--- Testing Performance Comparison ---");
        
        // Run performance comparison
        performanceAnalyzer.comparePerformance(testData);
    }
    
    /**
     * Test size impact analysis
     */
    private void testSizeImpactAnalysis(List<DataRecord> testData) {
        System.out.println("\n--- Testing Size Impact Analysis ---");
        
        // Run size impact analysis
        performanceAnalyzer.analyzeSizeImpact(testData);
    }
    
    /**
     * Test memory usage measurement
     */
    private void testMemoryUsage(List<DataRecord> testData) {
        System.out.println("\n--- Testing Memory Usage Measurement ---");
        
        // Run memory usage analysis
        performanceAnalyzer.measureMemoryUsage(testData);
    }
    
    /**
     * Test async processing functionality
     */
    private void testAsyncProcessing(List<DataRecord> testData) {
        System.out.println("\n--- Testing Async Processing ---");
        
        try {
            // Test basic async processing
            CompletableFuture<Map<String, Double>> future = asyncProcessor.processAsync(testData);
            Map<String, Double> result = future.get();
            System.out.println("Async processing completed. Categories found: " + result.size());
            
            // Test batch processing
            List<List<DataRecord>> batches = List.of(
                testData.subList(0, testData.size() / 2),
                testData.subList(testData.size() / 2, testData.size())
            );
            
            List<CompletableFuture<List<DataRecord>>> batchFutures = asyncProcessor.processBatch(batches);
            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (CompletableFuture<List<DataRecord>> batchFuture : batchFutures) {
                futures.add(batchFuture);
            }
            CompletableFuture<Map<String, Object>> combinedResult = asyncProcessor.combineResults(futures);
            
            Map<String, Object> batchResults = combinedResult.get();
            System.out.println("Batch processing completed. Batches processed: " + batchResults.size());
            
            // Test multiple operations
            CompletableFuture<Map<String, Object>> multiOpResult = asyncProcessor.processMultipleOperationsAsync(testData);
            Map<String, Object> multiOpResults = multiOpResult.get();
            System.out.println("Multiple operations completed. Results: " + multiOpResults.keySet());
            
        } catch (Exception e) {
            System.err.println("Error in async processing test: " + e.getMessage());
            e.printStackTrace();
        } finally {
            asyncProcessor.shutdown();
        }
    }
    
    /**
     * Test with different data sizes
     */
    public void testWithDifferentSizes() {
        System.out.println("\n=== Testing with Different Data Sizes ===");
        
        int[] sizes = {1000, 10000, 50000, 100000};
        
        for (int size : sizes) {
            System.out.println("\nTesting with " + size + " records:");
            
            List<DataRecord> data = dataGenerator.generateData(size);
            
            long startTime = System.currentTimeMillis();
            performanceAnalyzer.comparePerformance(data);
            long totalTime = System.currentTimeMillis() - startTime;
            
            System.out.println("Total test time: " + totalTime + "ms");
        }
    }
    
    /**
     * Test system information
     */
    public void testSystemInformation() {
        System.out.println("\n=== System Information ===");
        
        Runtime runtime = Runtime.getRuntime();
        System.out.println("Available processors: " + runtime.availableProcessors());
        System.out.println("Total memory: " + formatBytes(runtime.totalMemory()));
        System.out.println("Free memory: " + formatBytes(runtime.freeMemory()));
        System.out.println("Max memory: " + formatBytes(runtime.maxMemory()));
        System.out.println("Used memory: " + formatBytes(runtime.totalMemory() - runtime.freeMemory()));
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
     * Main method for running tests
     */
    public static void main(String[] args) {
        PerformanceTests tests = new PerformanceTests();
        tests.testSystemInformation();
        tests.runAllTests();
        tests.testWithDifferentSizes();
    }
}
