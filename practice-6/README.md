# Practice 6: Parallel Processing of Collections with Stream API

## Overview
This project implements parallel processing of collections using Java's Stream API, demonstrating various techniques for optimizing performance when processing large datasets.

## Project Structure
```
src/
├── main/
│   ├── java/
│   │   ├── model/
│   │   │   └── DataRecord.java          # Data model class
│   │   ├── processor/
│   │   │   ├── DataProcessor.java       # Basic parallel processing
│   │   │   ├── AdvancedProcessor.java   # Complex aggregation
│   │   │   └── AsyncProcessor.java      # Asynchronous processing
│   │   ├── performance/
│   │   │   └── PerformanceAnalyzer.java # Performance measurement
│   │   ├── util/
│   │   │   └── DataGenerator.java       # Test data generation
│   │   └── Demo.java                    # Main demonstration class
│   └── resources/
│       └── config.properties            # Configuration file
└── test/
    └── java/
        ├── processor/
        │   └── ProcessorTests.java      # Processor tests
        └── performance/
            └── PerformanceTests.java    # Performance tests
```

## Features Implemented

### Part 1: DataRecord Class
- Complete data model with all required fields (id, category, value, timestamp, priority, status, tags)
- Proper implementation of constructors, getters/setters, toString(), equals(), and hashCode()
- Support for complex data operations

### Part 2: Basic Parallel Processing (DataProcessor)
- **Sequential and Parallel Methods:**
  - `filterRecords()` / `filterRecordsParallel()` - Filter by category and value
  - `calculateStatistics()` / `calculateStatisticsParallel()` - Statistics by category
  - `groupByPriority()` / `groupByPriorityParallel()` - Group by priority
  - Additional utility methods for comprehensive testing

### Part 3: Complex Aggregation (AdvancedProcessor)
- `aggregateByCategories()` - Multi-level aggregation by category and priority
- `findTopNByCategory()` - Find top-N records by value in each category
- `analyzeByTimeIntervals()` - Statistical analysis by time intervals
- Advanced features: weighted averages, correlation analysis, outlier detection

### Part 4: Asynchronous Processing (AsyncProcessor)
- `processAsync()` - Asynchronous processing using CompletableFuture
- `processBatch()` - Parallel processing of multiple data sets
- `combineResults()` - Combining results from asynchronous operations
- Thread pool management and resource cleanup

### Part 5: Performance Analysis (PerformanceAnalyzer)
- `comparePerformance()` - Sequential vs parallel performance comparison
- `analyzeSizeImpact()` - Data size impact on performance
- `measureMemoryUsage()` - Memory usage analysis
- Comprehensive performance metrics and reporting

## Data Generation
- Generates minimum 1,000,000 records with diverse values
- Configurable categories, statuses, and value ranges
- Realistic timestamp generation and tag combinations
- Support for custom data characteristics

## Compilation and Execution

### Compile the project:
```bash
cd practice-6
javac -d . src/main/java/*.java src/main/java/*/*.java
```

### Run the demonstration:
```bash
java Demo
```

### Run individual tests:
```bash
# Processor tests
java -cp . processor.ProcessorTests

# Performance tests
java -cp . performance.PerformanceTests
```

## Performance Features
- **Parallel Stream Processing:** Utilizes `parallelStream()` for CPU-intensive operations
- **Asynchronous Processing:** Uses `CompletableFuture` for I/O-bound operations
- **Memory Optimization:** Efficient data structures and garbage collection awareness
- **Performance Measurement:** Comprehensive timing and memory usage analysis
- **Scalability Testing:** Tests with various data sizes to analyze performance scaling

## Configuration
The `config.properties` file allows customization of:
- Data generation parameters
- Performance measurement settings
- Thread pool configuration
- Memory management options
- Logging levels

## Key Learning Outcomes
1. **Stream API Mastery:** Understanding of sequential vs parallel stream processing
2. **Performance Optimization:** Techniques for optimizing large dataset processing
3. **Asynchronous Programming:** Using CompletableFuture for concurrent operations
4. **Memory Management:** Awareness of memory usage in parallel processing
5. **Performance Analysis:** Tools and techniques for measuring and comparing performance

## Additional Features
- Custom data generators with realistic test data
- Comprehensive error handling and resource management
- Detailed performance reporting and analysis
- Modular design for easy extension and testing
- Configuration-driven behavior for different environments

## Requirements Fulfilled
✅ DataRecord class with all required fields and methods  
✅ Test data generator creating 1,000,000+ records  
✅ DataProcessor with sequential and parallel method variants  
✅ AdvancedProcessor for complex aggregation operations  
✅ AsyncProcessor for asynchronous processing  
✅ PerformanceAnalyzer for comprehensive performance measurement  
✅ Complete project structure as specified  
✅ Configuration files and test classes  
✅ Demonstration and testing capabilities
