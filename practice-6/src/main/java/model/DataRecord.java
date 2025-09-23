package model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * DataRecord class representing a data record with various fields
 * for parallel processing with Stream API
 */
public class DataRecord {
    private long id;
    private String category;
    private double value;
    private LocalDateTime timestamp;
    private int priority;
    private String status;
    private List<String> tags;

    /**
     * Default constructor
     */
    public DataRecord() {
    }

    /**
     * Constructor with all parameters
     */
    public DataRecord(long id, String category, double value, LocalDateTime timestamp, 
                     int priority, String status, List<String> tags) {
        this.id = id;
        this.category = category;
        this.value = value;
        this.timestamp = timestamp;
        this.priority = priority;
        this.status = status;
        this.tags = tags;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "DataRecord{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", value=" + value +
                ", timestamp=" + timestamp +
                ", priority=" + priority +
                ", status='" + status + '\'' +
                ", tags=" + tags +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataRecord that = (DataRecord) o;
        return id == that.id &&
                Double.compare(that.value, value) == 0 &&
                priority == that.priority &&
                Objects.equals(category, that.category) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(status, that.status) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, category, value, timestamp, priority, status, tags);
    }
}
