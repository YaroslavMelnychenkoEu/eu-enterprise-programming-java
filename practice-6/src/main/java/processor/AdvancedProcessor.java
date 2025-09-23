package processor;

import model.DataRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.DoubleSummaryStatistics;

/**
 * AdvancedProcessor class for complex aggregation operations
 * Uses parallel processing to optimize performance
 */
public class AdvancedProcessor {

    /**
     * Complex aggregation by multiple parameters
     * Groups by category, then by priority, and calculates average value
     * @param records List of DataRecord objects
     * @return Map of category to Map of priority to average value
     */
    public Map<String, Map<Integer, Double>> aggregateByCategories(List<DataRecord> records) {
        return records.parallelStream()
                .collect(Collectors.groupingBy(
                        DataRecord::getCategory,
                        Collectors.groupingBy(
                                DataRecord::getPriority,
                                Collectors.averagingDouble(DataRecord::getValue)
                        )
                ));
    }

    /**
     * Find top-N records by value in each category
     * @param records List of DataRecord objects
     * @param n Number of top records to find
     * @return Map of category to list of top-N records
     */
    public Map<String, List<DataRecord>> findTopNByCategory(List<DataRecord> records, int n) {
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
    }

    /**
     * Statistical analysis by time intervals (daily)
     * @param records List of DataRecord objects
     * @return Map of date to statistics
     */
    public Map<LocalDate, DoubleSummaryStatistics> analyzeByTimeIntervals(List<DataRecord> records) {
        return records.parallelStream()
                .collect(Collectors.groupingBy(
                        record -> record.getTimestamp().toLocalDate(),
                        Collectors.summarizingDouble(DataRecord::getValue)
                ));
    }

    /**
     * Complex aggregation by category and status
     * @param records List of DataRecord objects
     * @return Map of category to Map of status to statistics
     */
    public Map<String, Map<String, DoubleSummaryStatistics>> aggregateByCategoryAndStatus(List<DataRecord> records) {
        return records.parallelStream()
                .collect(Collectors.groupingBy(
                        DataRecord::getCategory,
                        Collectors.groupingBy(
                                DataRecord::getStatus,
                                Collectors.summarizingDouble(DataRecord::getValue)
                        )
                ));
    }

    /**
     * Find records with specific tag combinations
     * @param records List of DataRecord objects
     * @param requiredTags List of tags that must be present
     * @return List of records containing all required tags
     */
    public List<DataRecord> findRecordsWithTags(List<DataRecord> records, List<String> requiredTags) {
        return records.parallelStream()
                .filter(record -> record.getTags() != null && 
                        record.getTags().containsAll(requiredTags))
                .collect(Collectors.toList());
    }

    /**
     * Calculate weighted average by priority
     * @param records List of DataRecord objects
     * @return Weighted average value
     */
    public double calculateWeightedAverage(List<DataRecord> records) {
        double weightedSum = records.parallelStream()
                .mapToDouble(record -> record.getValue() * record.getPriority())
                .sum();
        
        int totalWeight = records.parallelStream()
                .mapToInt(DataRecord::getPriority)
                .sum();
        
        return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
    }

    /**
     * Find records within value range by category
     * @param records List of DataRecord objects
     * @param minValue Minimum value
     * @param maxValue Maximum value
     * @return Map of category to list of records in range
     */
    public Map<String, List<DataRecord>> findRecordsInValueRange(List<DataRecord> records, 
                                                               double minValue, double maxValue) {
        return records.parallelStream()
                .filter(record -> record.getValue() >= minValue && record.getValue() <= maxValue)
                .collect(Collectors.groupingBy(DataRecord::getCategory));
    }

    /**
     * Calculate moving average for time series data
     * @param records List of DataRecord objects sorted by timestamp
     * @param windowSize Size of the moving window
     * @return Map of timestamp to moving average
     */
    public Map<LocalDateTime, Double> calculateMovingAverage(List<DataRecord> records, int windowSize) {
        List<DataRecord> sortedRecords = records.stream()
                .sorted(Comparator.comparing(DataRecord::getTimestamp))
                .collect(Collectors.toList());
        
        Map<LocalDateTime, Double> movingAverages = new HashMap<>();
        
        for (int i = windowSize - 1; i < sortedRecords.size(); i++) {
            double sum = sortedRecords.subList(i - windowSize + 1, i + 1)
                    .stream()
                    .mapToDouble(DataRecord::getValue)
                    .sum();
            movingAverages.put(sortedRecords.get(i).getTimestamp(), sum / windowSize);
        }
        
        return movingAverages;
    }

    /**
     * Find outliers using statistical methods
     * @param records List of DataRecord objects
     * @param threshold Standard deviation threshold (e.g., 2.0 for 2-sigma)
     * @return List of outlier records
     */
    public List<DataRecord> findOutliers(List<DataRecord> records, double threshold) {
        DoubleSummaryStatistics stats = records.parallelStream()
                .collect(Collectors.summarizingDouble(DataRecord::getValue));
        
        double mean = stats.getAverage();
        double stdDev = Math.sqrt(records.parallelStream()
                .mapToDouble(record -> Math.pow(record.getValue() - mean, 2))
                .average()
                .orElse(0.0));
        
        return records.parallelStream()
                .filter(record -> Math.abs(record.getValue() - mean) > threshold * stdDev)
                .collect(Collectors.toList());
    }

    /**
     * Complex correlation analysis between priority and value
     * @param records List of DataRecord objects
     * @return Correlation coefficient
     */
    public double calculatePriorityValueCorrelation(List<DataRecord> records) {
        double n = records.size();
        double sumX = records.parallelStream().mapToInt(DataRecord::getPriority).sum();
        double sumY = records.parallelStream().mapToDouble(DataRecord::getValue).sum();
        double sumXY = records.parallelStream().mapToDouble(record -> 
                record.getPriority() * record.getValue()).sum();
        double sumX2 = records.parallelStream().mapToDouble(record -> 
                record.getPriority() * record.getPriority()).sum();
        double sumY2 = records.parallelStream().mapToDouble(record -> 
                record.getValue() * record.getValue()).sum();
        
        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
        
        return denominator != 0 ? numerator / denominator : 0.0;
    }
}
