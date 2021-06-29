package neu.cs6650;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import neu.cs6650.model.LatencyRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReportWriter {
  private static final Logger logger = LogManager.getLogger(ReportWriter.class.getName());

  private String csvOutputFile;
  private List<LatencyRecord> latencyList;

  public ReportWriter(String csvOutputFile, List<LatencyRecord> latencyList) {
    this.csvOutputFile = csvOutputFile;
    this.latencyList = latencyList;
  }

  public void start() {
    File outputFile = new File(this.csvOutputFile);
    try (PrintWriter printWriter = new PrintWriter(outputFile)) {
      printWriter.println("start time, request type, latency, response code");
      this.latencyList.stream()
          .map(LatencyRecord::toString)
          .forEach(printWriter::println);
    } catch (FileNotFoundException e) {
      logger.info("Csv output file is not found");
    }
  }
}
