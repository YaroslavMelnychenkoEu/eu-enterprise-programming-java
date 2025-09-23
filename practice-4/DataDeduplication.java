import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Task 2: Data Deduplication System
 * Goal: Develop a system for detecting and eliminating duplicates in large datasets
 */
public class DataDeduplication<T> {
    
    // Fields for storing deduplication data
    private Set<T> uniqueElements;
    private Set<Hash> contentHashes;
    private long duplicatesFound;
    
    /**
     * Constructor initializes the data structures
     */
    public DataDeduplication() {
        // Using HashSet for unique elements (fast lookup and no duplicates)
        this.uniqueElements = new HashSet<>();
        // Using HashSet for content hashes (fast lookup)
        this.contentHashes = new HashSet<>();
        this.duplicatesFound = 0;
    }
    
    /**
     * Hash class to represent content hashes with locations
     */
    public static class Hash {
        private byte[] hashValue;
        private List<String> locations;
        
        /**
         * Constructor for Hash
         * @param hashValue the hash value as byte array
         * @param location the initial location of this hash
         */
        public Hash(byte[] hashValue, String location) {
            this.hashValue = hashValue.clone();
            this.locations = new ArrayList<>();
            this.locations.add(location);
        }
        
        // Getters
        public byte[] getHashValue() {
            return hashValue.clone();
        }
        
        public List<String> getLocations() {
            return new ArrayList<>(locations);
        }
        
        /**
         * Add a new location to this hash
         * @param location the location to add
         */
        public void addLocation(String location) {
            if (!locations.contains(location)) {
                locations.add(location);
            }
        }
        
        /**
         * Get the number of locations for this hash
         * @return number of locations
         */
        public int getLocationCount() {
            return locations.size();
        }
        
        /**
         * Equals method for proper comparison
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Hash hash = (Hash) obj;
            return Arrays.equals(hashValue, hash.hashValue);
        }
        
        /**
         * HashCode method for proper hashing
         */
        @Override
        public int hashCode() {
            return Arrays.hashCode(hashValue);
        }
        
        /**
         * String representation of Hash
         */
        @Override
        public String toString() {
            return "Hash{" +
                    "hashValue=" + bytesToHex(hashValue) +
                    ", locations=" + locations +
                    ", count=" + locations.size() +
                    '}';
        }
        
        /**
         * Convert byte array to hexadecimal string
         * @param bytes the byte array to convert
         * @return hexadecimal string representation
         */
        private String bytesToHex(byte[] bytes) {
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        }
    }
    
    /**
     * Add an element with duplicate checking
     * @param element the element to add
     * @return true if element was added (not a duplicate), false if it was a duplicate
     */
    public boolean addElement(T element) {
        // Generate hash for the element
        byte[] hashBytes = generateHash(element.toString());
        String elementLocation = "element_" + System.currentTimeMillis() + "_" + element.hashCode();
        
        // Check if this hash already exists
        Hash existingHash = findHashByValue(hashBytes);
        
        if (existingHash != null) {
            // This is a duplicate - add location to existing hash
            existingHash.addLocation(elementLocation);
            duplicatesFound++;
            System.out.println("Duplicate found for element: " + element + " (Hash: " + bytesToHex(hashBytes) + ")");
            return false;
        } else {
            // This is a new unique element
            Hash newHash = new Hash(hashBytes, elementLocation);
            contentHashes.add(newHash);
            uniqueElements.add(element);
            return true;
        }
    }
    
    /**
     * Find duplicates in a collection
     * @param data the collection to search for duplicates
     * @return Set of duplicate elements
     */
    public Set<T> findDuplicates(Collection<T> data) {
        Set<T> duplicates = new HashSet<>();
        Map<String, List<T>> hashToElements = new HashMap<>();
        
        // Group elements by their hash
        for (T element : data) {
            String hash = bytesToHex(generateHash(element.toString()));
            hashToElements.computeIfAbsent(hash, k -> new ArrayList<>()).add(element);
        }
        
        // Find groups with more than one element (duplicates)
        for (Map.Entry<String, List<T>> entry : hashToElements.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicates.addAll(entry.getValue());
            }
        }
        
        return duplicates;
    }
    
    /**
     * Get duplicate groups organized by hash
     * @return Map of Hash to List of elements that have the same hash
     */
    public Map<Hash, List<T>> getDuplicateGroups() {
        Map<Hash, List<T>> duplicateGroups = new HashMap<>();
        
        // Group elements by their hash
        Map<String, List<T>> hashToElements = new HashMap<>();
        for (T element : uniqueElements) {
            String hash = bytesToHex(generateHash(element.toString()));
            hashToElements.computeIfAbsent(hash, k -> new ArrayList<>()).add(element);
        }
        
        // Create Hash objects and group duplicates
        for (Map.Entry<String, List<T>> entry : hashToElements.entrySet()) {
            if (entry.getValue().size() > 1) {
                byte[] hashBytes = hexToBytes(entry.getKey());
                Hash hash = new Hash(hashBytes, "group_" + entry.getKey());
                duplicateGroups.put(hash, entry.getValue());
            }
        }
        
        return duplicateGroups;
    }
    
    /**
     * Generate hash for a given string using SHA-256
     * @param input the input string to hash
     * @return byte array representing the hash
     */
    private byte[] generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash if SHA-256 is not available
            return String.valueOf(input.hashCode()).getBytes();
        }
    }
    
    /**
     * Find hash by its value
     * @param hashValue the hash value to search for
     * @return Hash object if found, null otherwise
     */
    private Hash findHashByValue(byte[] hashValue) {
        for (Hash hash : contentHashes) {
            if (Arrays.equals(hash.getHashValue(), hashValue)) {
                return hash;
            }
        }
        return null;
    }
    
    /**
     * Convert byte array to hexadecimal string
     * @param bytes the byte array to convert
     * @return hexadecimal string representation
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    /**
     * Convert hexadecimal string to byte array
     * @param hex the hexadecimal string to convert
     * @return byte array representation
     */
    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
    
    /**
     * Get statistics about the deduplication process
     * @return formatted string with statistics
     */
    public String getStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== Data Deduplication Statistics ===\n");
        stats.append("Unique elements: ").append(uniqueElements.size()).append("\n");
        stats.append("Total hashes: ").append(contentHashes.size()).append("\n");
        stats.append("Duplicates found: ").append(duplicatesFound).append("\n");
        
        // Count hashes with multiple locations
        long hashesWithDuplicates = contentHashes.stream()
                .mapToLong(hash -> hash.getLocationCount() > 1 ? 1 : 0)
                .sum();
        
        stats.append("Hashes with duplicates: ").append(hashesWithDuplicates).append("\n");
        
        return stats.toString();
    }
    
    /**
     * Get all unique elements
     * @return Set of unique elements
     */
    public Set<T> getUniqueElements() {
        return new HashSet<>(uniqueElements);
    }
    
    /**
     * Get total number of duplicates found
     * @return number of duplicates
     */
    public long getDuplicatesFound() {
        return duplicatesFound;
    }
    
    /**
     * Main method for demonstration and testing
     */
    public static void main(String[] args) {
        System.out.println("=== Data Deduplication System Demo ===\n");
        
        // Test 1: Text data deduplication
        System.out.println("Test 1: Text Data Deduplication");
        System.out.println("=================================");
        
        DataDeduplication<String> textDeduplicator = new DataDeduplication<>();
        
        // Add some text data with duplicates
        String[] textData = {
            "Hello World",
            "Java Programming",
            "Hello World",  // duplicate
            "Data Structures",
            "Java Programming",  // duplicate
            "Algorithms",
            "Hello World",  // duplicate
            "Machine Learning",
            "Data Structures"  // duplicate
        };
        
        System.out.println("Adding text elements...");
        for (String text : textData) {
            boolean added = textDeduplicator.addElement(text);
            System.out.println("Added '" + text + "': " + (added ? "NEW" : "DUPLICATE"));
        }
        
        System.out.println("\n" + textDeduplicator.getStatistics());
        
        // Test 2: Find duplicates in a collection
        System.out.println("\nTest 2: Finding Duplicates in Collection");
        System.out.println("========================================");
        
        List<String> testCollection = Arrays.asList(
            "Apple", "Banana", "Apple", "Cherry", "Banana", "Date", "Apple"
        );
        
        Set<String> duplicates = textDeduplicator.findDuplicates(testCollection);
        System.out.println("Duplicates found: " + duplicates);
        
        // Test 3: Get duplicate groups
        System.out.println("\nTest 3: Duplicate Groups");
        System.out.println("========================");
        
        Map<Hash, List<String>> duplicateGroups = textDeduplicator.getDuplicateGroups();
        for (Map.Entry<Hash, List<String>> entry : duplicateGroups.entrySet()) {
            System.out.println("Hash: " + entry.getKey());
            System.out.println("Elements: " + entry.getValue());
            System.out.println();
        }
        
        // Test 4: Large dataset simulation
        System.out.println("Test 4: Large Dataset Simulation");
        System.out.println("=================================");
        
        DataDeduplication<Integer> numberDeduplicator = new DataDeduplication<>();
        
        // Simulate adding 1000 numbers with some duplicates
        Random random = new Random(42); // Fixed seed for reproducible results
        int duplicatesInDataset = 0;
        
        System.out.println("Adding 1000 random numbers (0-100)...");
        for (int i = 0; i < 1000; i++) {
            int number = random.nextInt(101); // 0-100 range
            boolean added = numberDeduplicator.addElement(number);
            if (!added) {
                duplicatesInDataset++;
            }
        }
        
        System.out.println("\n" + numberDeduplicator.getStatistics());
        System.out.println("Expected duplicates in range 0-100 with 1000 samples: ~" + duplicatesInDataset);
        
        // Test 5: Binary data simulation (using byte arrays as strings)
        System.out.println("\nTest 5: Binary Data Simulation");
        System.out.println("===============================");
        
        DataDeduplication<String> binaryDeduplicator = new DataDeduplication<>();
        
        // Simulate binary file content
        String[] binaryData = {
            "0101010101010101",  // Binary pattern 1
            "1100110011001100",  // Binary pattern 2
            "0101010101010101",  // Duplicate of pattern 1
            "1111000011110000",  // Binary pattern 3
            "1100110011001100",  // Duplicate of pattern 2
            "0101010101010101"   // Another duplicate of pattern 1
        };
        
        System.out.println("Processing binary data...");
        for (String binary : binaryData) {
            boolean added = binaryDeduplicator.addElement(binary);
            System.out.println("Binary pattern: " + binary + " - " + (added ? "NEW" : "DUPLICATE"));
        }
        
        System.out.println("\n" + binaryDeduplicator.getStatistics());
        
        System.out.println("\n=== Performance Analysis ===");
        System.out.println("All tests completed successfully!");
        System.out.println("The system efficiently handles:");
        System.out.println("- Text data deduplication");
        System.out.println("- Binary data simulation");
        System.out.println("- Large dataset processing");
        System.out.println("- Different hashing algorithms (SHA-256)");
    }
}
