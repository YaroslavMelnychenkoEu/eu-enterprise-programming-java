import java.sql.Timestamp;
import java.util.*;

/**
 * Task 1: Implementation of a network traffic monitoring system
 * Goal: Develop a system for analyzing and filtering network traffic using different Set implementations
 */
public class NetworkTrafficAnalyzer {
    
    // Fields for storing network traffic data
    private Set<IpAddress> uniqueIps;
    private Set<String> suspiciousPatterns;
    private Map<IpAddress, Integer> requestCount;
    
    /**
     * Constructor initializes the data structures
     */
    public NetworkTrafficAnalyzer() {
        // Using HashSet for unique IPs (fast lookup and no duplicates)
        this.uniqueIps = new HashSet<>();
        // Using HashSet for suspicious patterns (fast lookup)
        this.suspiciousPatterns = new HashSet<>();
        // Using HashMap for request counting (fast key-value operations)
        this.requestCount = new HashMap<>();
    }
    
    /**
     * IpAddress class to represent IP addresses with timestamps
     */
    public static class IpAddress {
        private final String ip;
        private final Timestamp lastAccess;
        
        /**
         * Constructor for IpAddress
         * @param ip the IP address string
         * @param lastAccess the timestamp of last access
         */
        public IpAddress(String ip, Timestamp lastAccess) {
            this.ip = ip;
            this.lastAccess = lastAccess;
        }
        
        // Getters
        public String getIp() {
            return ip;
        }
        
        public Timestamp getLastAccess() {
            return lastAccess;
        }
        
        /**
         * Equals method for proper comparison
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            IpAddress ipAddress = (IpAddress) obj;
            return Objects.equals(ip, ipAddress.ip);
        }
        
        /**
         * HashCode method for proper hashing
         */
        @Override
        public int hashCode() {
            return Objects.hash(ip);
        }
        
        /**
         * String representation of IpAddress
         */
        @Override
        public String toString() {
            return "IpAddress{" +
                    "ip='" + ip + '\'' +
                    ", lastAccess=" + lastAccess +
                    '}';
        }
    }
    
    /**
     * Process a network request
     * @param ipAddress the IP address making the request
     * @param requestData the request data/content
     */
    public void processRequest(String ipAddress, String requestData) {
        // Create timestamp for current request
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        
        // Create IpAddress object
        IpAddress ip = new IpAddress(ipAddress, currentTime);
        
        // Add to unique IPs set (automatically handles duplicates)
        uniqueIps.add(ip);
        
        // Update request count
        requestCount.put(ip, requestCount.getOrDefault(ip, 0) + 1);
        
        // Check for suspicious patterns in request data
        if (containsSuspiciousPattern(requestData)) {
            System.out.println("Suspicious request detected from " + ipAddress + ": " + requestData);
        }
        
        System.out.println("Processed request from " + ipAddress + " at " + currentTime);
    }
    
    /**
     * Check if an IP address is blacklisted
     * @param ipAddress the IP address to check
     * @return true if the IP is blacklisted, false otherwise
     */
    public boolean isBlacklisted(String ipAddress) {
        // Check if IP exists in our unique IPs and has suspicious activity
        for (IpAddress ip : uniqueIps) {
            if (ip.getIp().equals(ipAddress)) {
                // Check if this IP has made too many requests (threshold: 100)
                int requests = requestCount.getOrDefault(ip, 0);
                if (requests > 100) {
                    return true;
                }
                
                // Check if IP matches any suspicious patterns
                if (suspiciousPatterns.contains(ipAddress)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Get IP addresses with high load (above threshold)
     * @param threshold the minimum number of requests to be considered high load
     * @return Set of IP addresses with high load
     */
    public Set<IpAddress> getHighLoadIps(int threshold) {
        Set<IpAddress> highLoadIps = new HashSet<>();
        
        // Iterate through request counts and find IPs above threshold
        for (Map.Entry<IpAddress, Integer> entry : requestCount.entrySet()) {
            if (entry.getValue() > threshold) {
                highLoadIps.add(entry.getKey());
            }
        }
        
        return highLoadIps;
    }
    
    /**
     * Add a suspicious pattern to monitor
     * @param pattern the suspicious pattern to add
     */
    public void addSuspiciousPattern(String pattern) {
        suspiciousPatterns.add(pattern);
    }
    
    /**
     * Check if request data contains suspicious patterns
     * @param requestData the request data to check
     * @return true if suspicious pattern found, false otherwise
     */
    private boolean containsSuspiciousPattern(String requestData) {
        for (String pattern : suspiciousPatterns) {
            if (requestData.toLowerCase().contains(pattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get statistics about the network traffic
     * @return formatted string with statistics
     */
    public String getStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== Network Traffic Statistics ===\n");
        stats.append("Total unique IPs: ").append(uniqueIps.size()).append("\n");
        stats.append("Total requests processed: ").append(requestCount.values().stream().mapToInt(Integer::intValue).sum()).append("\n");
        stats.append("Suspicious patterns monitored: ").append(suspiciousPatterns.size()).append("\n");
        
        // Find IP with most requests
        Optional<Map.Entry<IpAddress, Integer>> maxRequests = requestCount.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue());
        
        if (maxRequests.isPresent()) {
            stats.append("IP with most requests: ").append(maxRequests.get().getKey().getIp())
                 .append(" (").append(maxRequests.get().getValue()).append(" requests)\n");
        }
        
        return stats.toString();
    }
    
    /**
     * Main method for demonstration
     */
    public static void main(String[] args) {
        System.out.println("=== Network Traffic Monitoring System Demo ===\n");
        
        // Create analyzer instance
        NetworkTrafficAnalyzer analyzer = new NetworkTrafficAnalyzer();
        
        // Add some suspicious patterns
        analyzer.addSuspiciousPattern("malware");
        analyzer.addSuspiciousPattern("hack");
        analyzer.addSuspiciousPattern("exploit");
        analyzer.addSuspiciousPattern("virus");
        
        // Simulate some network requests
        System.out.println("Processing sample requests...\n");
        
        // Normal requests
        analyzer.processRequest("192.168.1.1", "GET /index.html HTTP/1.1");
        analyzer.processRequest("192.168.1.2", "POST /login HTTP/1.1");
        analyzer.processRequest("192.168.1.3", "GET /images/logo.png HTTP/1.1");
        
        // Some requests from the same IP (to test high load detection)
        for (int i = 0; i < 5; i++) {
            analyzer.processRequest("192.168.1.1", "GET /page" + i + ".html HTTP/1.1");
        }
        
        // Suspicious request
        analyzer.processRequest("10.0.0.1", "GET /malware.exe HTTP/1.1");
        
        // More requests to test blacklist functionality
        for (int i = 0; i < 105; i++) {
            analyzer.processRequest("192.168.1.100", "GET /test" + i + ".html HTTP/1.1");
        }
        
        System.out.println("\n" + analyzer.getStatistics());
        
        // Test blacklist functionality
        System.out.println("=== Blacklist Testing ===");
        System.out.println("Is 192.168.1.100 blacklisted? " + analyzer.isBlacklisted("192.168.1.100"));
        System.out.println("Is 192.168.1.1 blacklisted? " + analyzer.isBlacklisted("192.168.1.1"));
        System.out.println("Is 10.0.0.1 blacklisted? " + analyzer.isBlacklisted("10.0.0.1"));
        
        // Test high load IPs
        System.out.println("\n=== High Load IPs (threshold: 3) ===");
        Set<NetworkTrafficAnalyzer.IpAddress> highLoadIps = analyzer.getHighLoadIps(3);
        for (NetworkTrafficAnalyzer.IpAddress ip : highLoadIps) {
            System.out.println(ip.getIp() + " - " + analyzer.requestCount.get(ip) + " requests");
        }
        
        System.out.println("\n=== High Load IPs (threshold: 100) ===");
        Set<NetworkTrafficAnalyzer.IpAddress> veryHighLoadIps = analyzer.getHighLoadIps(100);
        for (NetworkTrafficAnalyzer.IpAddress ip : veryHighLoadIps) {
            System.out.println(ip.getIp() + " - " + analyzer.requestCount.get(ip) + " requests");
        }
    }
}
