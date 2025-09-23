package processor;

import model.DataRecord;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * AsyncProcessor class for asynchronous processing using CompletableFuture
 * Provides parallel processing of multiple data sets and result combination
 */
public class AsyncProcessor {
    
    private final ExecutorService executorService;
    
    public AsyncProcessor() {
        this.executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors());
    }
    
    public AsyncProcessor(int threadPoolSize) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    /**
     * Asynchronous processing using CompletableFuture
     * Processes records and returns aggregated results by category
     * @param records List of DataRecord objects
     * @return CompletableFuture with Map of category to average value
     */
    public CompletableFuture<Map<String, Double>> processAsync(List<DataRecord> records) {
        return CompletableFuture.supplyAsync(() -> {
            return records.parallelStream()
                    .collect(Collectors.groupingBy(
                            DataRecord::getCategory,
                            Collectors.averagingDouble(DataRecord::getValue)
                    ));
        }, executorService);
    }

    /**
     * Parallel processing of multiple data sets
     * @param batches List of data batches to process
     * @return List of CompletableFuture objects for each batch
     */
    public List<CompletableFuture<List<DataRecord>>> processBatch(List<List<DataRecord>> batches) {
        return batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(() -> {
                    // Simulate some processing - filter high priority records
                    return batch.parallelStream()
                            .filter(record -> record.getPriority() >= 3)
                            .collect(Collectors.toList());
                }, executorService))
                .collect(Collectors.toList());
    }

    /**
     * Combine results of asynchronous processing
     * @param futures List of CompletableFuture objects
     * @return CompletableFuture with combined results
     */
    public CompletableFuture<Map<String, Object>> combineResults(List<CompletableFuture<?>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    Map<String, Object> results = new HashMap<>();
                    
                    for (int i = 0; i < futures.size(); i++) {
                        try {
                            Object result = futures.get(i).get();
                            results.put("batch_" + i, result);
                        } catch (Exception e) {
                            results.put("batch_" + i + "_error", e.getMessage());
                        }
                    }
                    
                    return results;
                });
    }

    /**
     * Asynchronous filtering with custom criteria
     * @param records List of DataRecord objects
     * @param category Category to filter by
     * @param minValue Minimum value threshold
     * @return CompletableFuture with filtered results
     */
    public CompletableFuture<List<DataRecord>> filterAsync(List<DataRecord> records, 
                                                          String category, double minValue) {
        return CompletableFuture.supplyAsync(() -> {
            return records.parallelStream()
                    .filter(record -> category.equals(record.getCategory()))
                    .filter(record -> record.getValue() >= minValue)
                    .collect(Collectors.toList());
        }, executorService);
    }

    /**
     * Asynchronous statistical analysis
     * @param records List of DataRecord objects
     * @return CompletableFuture with statistics
     */
    public CompletableFuture<Map<String, Object>> analyzeAsync(List<DataRecord> records) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> analysis = new HashMap<>();
            
            // Calculate basic statistics
            DoubleSummaryStatistics stats = records.parallelStream()
                    .collect(Collectors.summarizingDouble(DataRecord::getValue));
            
            analysis.put("count", stats.getCount());
            analysis.put("sum", stats.getSum());
            analysis.put("min", stats.getMin());
            analysis.put("max", stats.getMax());
            analysis.put("average", stats.getAverage());
            
            // Count by category
            Map<String, Long> categoryCount = records.parallelStream()
                    .collect(Collectors.groupingBy(
                            DataRecord::getCategory,
                            Collectors.counting()
                    ));
            analysis.put("categoryCount", categoryCount);
            
            // Count by status
            Map<String, Long> statusCount = records.parallelStream()
                    .collect(Collectors.groupingBy(
                            DataRecord::getStatus,
                            Collectors.counting()
                    ));
            analysis.put("statusCount", statusCount);
            
            return analysis;
        }, executorService);
    }

    /**
     * Asynchronous top-N analysis by category
     * @param records List of DataRecord objects
     * @param n Number of top records
     * @return CompletableFuture with top-N results
     */
    public CompletableFuture<Map<String, List<DataRecord>>> findTopNAsync(List<DataRecord> records, int n) {
        return CompletableFuture.supplyAsync(() -> {
            return records.parallelStream()
                    .collect(Collectors.groupingBy(
                            DataRecord::getCategory,
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    list -> list.stream()
                                            .sorted((r1, r2) -> Double.compare(r2.getValue(), r1.getValue()))
                                            .limit(n)
                                            .collect(Collectors.toList())
                            )
                    ));
        }, executorService);
    }

    /**
     * Process multiple operations in parallel and combine results
     * @param records List of DataRecord objects
     * @return CompletableFuture with combined analysis
     */
    public CompletableFuture<Map<String, Object>> processMultipleOperationsAsync(List<DataRecord> records) {
        CompletableFuture<Map<String, Double>> categoryAverages = processAsync(records);
        CompletableFuture<Map<String, Object>> analysis = analyzeAsync(records);
        CompletableFuture<Map<String, List<DataRecord>>> topRecords = findTopNAsync(records, 5);
        
        return CompletableFuture.allOf(categoryAverages, analysis, topRecords)
                .thenApply(v -> {
                    Map<String, Object> combinedResults = new HashMap<>();
                    try {
                        combinedResults.put("categoryAverages", categoryAverages.get());
                        combinedResults.put("analysis", analysis.get());
                        combinedResults.put("topRecords", topRecords.get());
                    } catch (Exception e) {
                        combinedResults.put("error", e.getMessage());
                    }
                    return combinedResults;
                });
    }

    /**
     * Shutdown the executor service
     */
    public void shutdown() {
        executorService.shutdown();
    }

    /**
     * Check if executor service is shutdown
     * @return true if shutdown, false otherwise
     */
    public boolean isShutdown() {
        return executorService.isShutdown();
    }
}
