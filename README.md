# Multithreaded Java Client
A multithreaded client to test and measure performance of a distributed system hosted on AWS.

## Important Key Designs
1. OkHttp3 client implementation
2. Thread pools and executor to allocated tasks
3. CompletionService to automate result retrieval
4. Producer - Consumer model (blockingQueue)
5. Log4j2 for logging

## Performance Metrics
1. Total number of successful and failed requests
2. Mean, Median, p99, Max latency for POST requests
3. Total wall time
4. Throughput / request per second
5. CSV report of individual latency records

## Testings
Testings are done for various AWS EC2 configurations and number of client's threads (n = 32, 64, 128, 256).


## Dependencies


## Deployment on AWS


## Built With
* [Maven](https://maven.apache.org/) - Dependency Management
