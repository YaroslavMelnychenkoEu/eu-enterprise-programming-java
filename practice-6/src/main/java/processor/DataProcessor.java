package processor;

import model.DataRecord;
import java.util.List;
import java.util.Map;
import java.util.DoubleSummaryStatistics;
import java.util.stream.Collectors;

/**
 * DataProcessor class for basic parallel processing operations
 * Implements methods in both sequential and parallel variants
 */
public class DataProcessor {

    /**
     * Filter records by category and minimum value (Sequential)
     * @param records List of DataRecord objects
     * @param category Category to filter by
     * @param minValue Minimum value threshold
     * @return Filtered list of records
     */
    public List<DataRecord> filterRecords(List<DataRecord> records, String category, double minValue) {
        return records.stream()
                .filter(record -> category.equals(record.getCategory()))
                .filter(record -> record.getValue() >= minValue)
                .collect(Collectors.toList());
    }

    /**
     * Filter records by category and minimum value (Parallel)
     * @param records List of DataRecord objects
     * @param category Category to filter by
     * @param minValue Minimum value threshold
     * @return Filtered list of records
     */
    public List<DataRecord> filterRecordsParallel(List<DataRecord> records, String category, double minValue) {
        return records.parallelStream()
                .filter(record -> category.equals(record.getCategory()))
                .filter(record -> record.getValue() >= minValue)
                .collect(Collectors.toList());
    }

    /**
     * Calculate statistics by categories (Sequential)
     * @param records List of DataRecord objects
     * @return Map of category to statistics
     */
    public Map<String, DoubleSummaryStatistics> calculateStatistics(List<DataRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(
                        DataRecord::getCategory,
                        Collectors.summarizingDouble(DataRecord::getValue)
                ));
    }

    /**
     * Calculate statistics by categories (Parallel)
     * @param records List of DataRecord objects
     * @return Map of category to statistics
     */
    public Map<String, DoubleSummaryStatistics> calculateStatisticsParallel(List<DataRecord> records) {
        return records.parallelStream()
                .collect(Collectors.groupingBy(
                        DataRecord::getCategory,
                        Collectors.summarizingDouble(DataRecord::getValue)
                ));
    }

    /**
     * Group records by priority (Sequential)
     * @param records List of DataRecord objects
     * @return Map of priority to list of records
     */
    public Map<Integer, List<DataRecord>> groupByPriority(List<DataRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(DataRecord::getPriority));
    }

    /**
     * Group records by priority (Parallel)
     * @param records List of DataRecord objects
     * @return Map of priority to list of records
     */
    public Map<Integer, List<DataRecord>> groupByPriorityParallel(List<DataRecord> records) {
        return records.parallelStream()
                .collect(Collectors.groupingBy(DataRecord::getPriority));
    }

    /**
     * Filter records by status (Sequential)
     * @param records List of DataRecord objects
     * @param status Status to filter by
     * @return Filtered list of records
     */
    public List<DataRecord> filterByStatus(List<DataRecord> records, String status) {
        return records.stream()
                .filter(record -> status.equals(record.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Filter records by status (Parallel)
     * @param records List of DataRecord objects
     * @param status Status to filter by
     * @return Filtered list of records
     */
    public List<DataRecord> filterByStatusParallel(List<DataRecord> records, String status) {
        return records.parallelStream()
                .filter(record -> status.equals(record.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Calculate average value by category (Sequential)
     * @param records List of DataRecord objects
     * @return Map of category to average value
     */
    public Map<String, Double> calculateAverageByCategory(List<DataRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(
                        DataRecord::getCategory,
                        Collectors.averagingDouble(DataRecord::getValue)
                ));
    }

    /**
     * Calculate average value by category (Parallel)
     * @param records List of DataRecord objects
     * @return Map of category to average value
     */
    public Map<String, Double> calculateAverageByCategoryParallel(List<DataRecord> records) {
        return records.parallelStream()
                .collect(Collectors.groupingBy(
                        DataRecord::getCategory,
                        Collectors.averagingDouble(DataRecord::getValue)
                ));
    }

    /**
     * Count records by status (Sequential)
     * @param records List of DataRecord objects
     * @return Map of status to count
     */
    public Map<String, Long> countByStatus(List<DataRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(
                        DataRecord::getStatus,
                        Collectors.counting()
                ));
    }

    /**
     * Count records by status (Parallel)
     * @param records List of DataRecord objects
     * @return Map of status to count
     */
    public Map<String, Long> countByStatusParallel(List<DataRecord> records) {
        return records.parallelStream()
                .collect(Collectors.groupingBy(
                        DataRecord::getStatus,
                        Collectors.counting()
                ));
    }
}
