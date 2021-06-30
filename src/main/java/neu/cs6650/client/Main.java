package neu.cs6650.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import neu.cs6650.Configuration;
import neu.cs6650.InputProcessor;
import neu.cs6650.ReportWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
  private static final Logger logger = LogManager.getLogger(Main.class.getName());
//  private static final String INPUT_PATH = "test.txt";
//  private static final String INPUT_PATH = "bsds-summer-2021-testdata.txt";
  private static final String INPUT_PATH = "bsds-summer-2021-testdata-assignment2.txt";
  private static final String POISON_PILL = "-1";
  private static String AWS_API_ROUTE;

  public static void main(String[] args) throws IOException, InterruptedException {
    // Create a BlockingQ
    // Create a file reader -> put in Blocking Q
    // Start the thread pool -> pulling from the Blocking Q
    // Retrieve the results
    // Write to CSV
    // Process Statistics

    Properties prop = new Properties();
    try (InputStream input = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("config.properties")) {
      prop.load(input);
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    AWS_API_ROUTE = prop.getProperty("aws.aws_api_address");

    String ipAddress = Configuration.IS_LOCAL ? "localhost" : AWS_API_ROUTE;
    String port = "8080";
    String function = "wordcount";
    String outputPath = "/Users/tmarthin/Code/tiffanymarthin/distributed-systems/assignment1/multithreaded-client-p2/output/sample.csv";
    int maxThread = 64;

    BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();

    long startTime, endTime;

    // Command line arguments for easier tests
    //TODO validates the inputs
    if (args.length != 0) {
      outputPath = args[0];
      ipAddress = args[1];
      maxThread = Integer.parseInt(args[2]);
    }

    logger.info("*********** Parameters ***********");
    logger.info("Number of threads: " + maxThread);

    logger.info("*********** Input Processor Starts ***********");
    InputProcessor inputProcessor = new InputProcessor(INPUT_PATH, blockingQueue, maxThread, POISON_PILL);
    new Thread(inputProcessor).start();

    logger.info("*********** Client Starts ***********");
    MultithreadedClient client = new MultithreadedClient(maxThread, blockingQueue, ipAddress, port, function, POISON_PILL);
    startTime = System.currentTimeMillis();
    client.start();
    endTime = System.currentTimeMillis();
    logger.info("*********** Client Ends ***********\n");

    logger.info("*********** Processing Statistics ***********");
//    logger.info("Input processing run time: " + (inputProcessor.getEndTime() - inputProcessor.getStartTime()));
    logger.info("Number of successful requests: " + client.getTotalSuccessfulRequests());
    logger.info("Number of failed requests: " + client.getTotalFailedRequests());
    logger.info("Client run time: " + (endTime - startTime) + " milliseconds");
//    logger.info("Throughput (requests/second): " + ((client.getTotalSuccessfulRequests() + client.getTotalFailedRequests()) * 1000 / (endTime - startTime)));
    logger.info("Throughput (requests/second): " + (Util.requestsPerSecond((client.getTotalSuccessfulRequests() + client.getTotalFailedRequests()), (endTime - startTime))) + "\n");

//    logger.info("*********** Write CSV Output for Latency Records ***********");
    ReportWriter reportWriter = new ReportWriter(outputPath, client.getLatencyList());
    reportWriter.start();
//    logger.info("*********** CSV Writer Output for Latency Records ***********");

    logger.info("Num of cores: " + Runtime.getRuntime().availableProcessors());
    logger.info("*********** Thread Aggregate Statistics ***********");
    logger.info("Mean response time for POSTs (milliseconds): " + Util.meanResponseTime(client.getLatencyList()));
    logger.info("Median response time for POSTs (milliseconds): " + Util.medianResponseTime(client.getLatencyList()));
    logger.info("25th percentile response time for POSTs (milliseconds): " + Util.p25ResponseTime(client.getLatencyList()));
    logger.info("75th percentile response time for POSTs (milliseconds): " + Util.p75ResponseTime(client.getLatencyList()));
    logger.info("99th percentile response time for POSTs (milliseconds): " + Util.p99ResponseTime(client.getLatencyList()));
    logger.info("Max response time for POSTs (milliseconds): " + Util.maxResponseTime(client.getLatencyList()));
  }
}
