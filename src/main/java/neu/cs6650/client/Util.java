package neu.cs6650.client;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.DoubleStream;
import neu.cs6650.model.LatencyRecord;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;


public class Util {
  public static double meanResponseTime(List<LatencyRecord> latencyList) {
    return latencyList.stream()
        .mapToDouble(LatencyRecord::getLatency)
        .average()
        .orElseThrow(NoSuchElementException::new);
  }

  public static double medianResponseTime(List<LatencyRecord> latencyList) {
    DoubleStream sortedLatencies = latencyList.stream().mapToDouble(LatencyRecord::getLatency).sorted();
    double median = latencyList.size() % 2 == 0 ?
        sortedLatencies.skip(latencyList.size() / 2 - 1).limit(2).average().getAsDouble() :
        sortedLatencies.skip(latencyList.size() / 2).findFirst().getAsDouble();
    return median;
  }

  public static double requestsPerSecond(long totalRequests, long wallTime) {
    return totalRequests * 1000.0 / wallTime;
  }

  public static double p99ResponseTime(List<LatencyRecord> latencyList) {
    Percentile p99 = new Percentile();
    DoubleStream sortedLatencies = latencyList.stream().mapToDouble(LatencyRecord::getLatency).sorted();

    return p99.evaluate(sortedLatencies.toArray(), 99);
  }

  public static double maxResponseTime(List<LatencyRecord> latencyList) {
    return latencyList.stream()
        .mapToDouble(LatencyRecord::getLatency)
        .max()
        .orElseThrow(NoSuchElementException::new);
  }

  public static double p25ResponseTime(List<LatencyRecord> latencyList) {
    Percentile p25 = new Percentile();
    DoubleStream sortedLatencies = latencyList.stream().mapToDouble(LatencyRecord::getLatency).sorted();

    return p25.evaluate(sortedLatencies.toArray(), 25);
  }

  public static double p75ResponseTime(List<LatencyRecord> latencyList) {
    Percentile p75 = new Percentile();
    DoubleStream sortedLatencies = latencyList.stream().mapToDouble(LatencyRecord::getLatency).sorted();

    return p75.evaluate(sortedLatencies.toArray(), 75);
  }
}
