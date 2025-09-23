package util;

import model.DataRecord;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * DataGenerator class for creating test data collections
 * Generates at least 1,000,000 records with different values
 */
public class DataGenerator {
    
    private static final String[] CATEGORIES = {
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J"
    };
    
    private static final String[] STATUSES = {
        "ACTIVE", "INACTIVE", "PENDING", "COMPLETED", "FAILED"
    };
    
    private static final String[] TAG_POOL = {
        "urgent", "normal", "low", "high", "critical", "maintenance", 
        "update", "delete", "create", "read", "write", "process"
    };
    
    private final Random random = new Random();
    
    /**
     * Generate a collection of DataRecord objects
     * @param count Number of records to generate (minimum 1,000,000)
     * @return List of DataRecord objects
     */
    public List<DataRecord> generateData(int count) {
        if (count < 1_000_000) {
            count = 1_000_000;
        }
        
        System.out.println("Generating " + count + " data records...");
        
        return IntStream.range(0, count)
                .parallel()
                .mapToObj(this::generateRecord)
                .collect(Collectors.toList());
    }
    
    /**
     * Generate a single DataRecord
     * @param index Index of the record
     * @return Generated DataRecord
     */
    private DataRecord generateRecord(int index) {
        long id = index + 1;
        String category = CATEGORIES[random.nextInt(CATEGORIES.length)];
        double value = ThreadLocalRandom.current().nextDouble(0.0, 10000.0);
        LocalDateTime timestamp = generateRandomTimestamp();
        int priority = random.nextInt(1, 6); // 1-5
        String status = STATUSES[random.nextInt(STATUSES.length)];
        List<String> tags = generateRandomTags();
        
        return new DataRecord(id, category, value, timestamp, priority, status, tags);
    }
    
    /**
     * Generate a random timestamp within the last year
     * @return Random LocalDateTime
     */
    private LocalDateTime generateRandomTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneYearAgo = now.minus(365, ChronoUnit.DAYS);
        
        long daysBetween = ChronoUnit.DAYS.between(oneYearAgo, now);
        long randomDays = ThreadLocalRandom.current().nextLong(0, daysBetween + 1);
        
        return oneYearAgo.plusDays(randomDays)
                .plusHours(ThreadLocalRandom.current().nextLong(0, 24))
                .plusMinutes(ThreadLocalRandom.current().nextLong(0, 60))
                .plusSeconds(ThreadLocalRandom.current().nextLong(0, 60));
    }
    
    /**
     * Generate a random list of tags
     * @return List of random tags
     */
    private List<String> generateRandomTags() {
        int tagCount = random.nextInt(1, 5); // 1-4 tags
        Set<String> selectedTags = new HashSet<>();
        
        while (selectedTags.size() < tagCount) {
            selectedTags.add(TAG_POOL[random.nextInt(TAG_POOL.length)]);
        }
        
        return new ArrayList<>(selectedTags);
    }
    
    /**
     * Generate data with specific characteristics for testing
     * @param count Number of records
     * @param categoryFilter Specific category to focus on
     * @param valueRangeMin Minimum value
     * @param valueRangeMax Maximum value
     * @return List of DataRecord objects
     */
    public List<DataRecord> generateDataWithCharacteristics(int count, String categoryFilter, 
                                                           double valueRangeMin, double valueRangeMax) {
        return IntStream.range(0, count)
                .parallel()
                .mapToObj(i -> {
                    long id = i + 1;
                    String category = categoryFilter != null ? categoryFilter : CATEGORIES[random.nextInt(CATEGORIES.length)];
                    double value = ThreadLocalRandom.current().nextDouble(valueRangeMin, valueRangeMax);
                    LocalDateTime timestamp = generateRandomTimestamp();
                    int priority = random.nextInt(1, 6);
                    String status = STATUSES[random.nextInt(STATUSES.length)];
                    List<String> tags = generateRandomTags();
                    
                    return new DataRecord(id, category, value, timestamp, priority, status, tags);
                })
                .collect(Collectors.toList());
    }
}
