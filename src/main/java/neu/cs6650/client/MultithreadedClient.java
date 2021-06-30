package neu.cs6650.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import neu.cs6650.model.LatencyRecord;
import neu.cs6650.model.ThreadInput;
import neu.cs6650.model.ThreadRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MultithreadedClient {

  private static final Logger logger = LogManager.getLogger(PostCallable.class.getName());

  private Integer maxThreads;
  private BlockingQueue<String> lineQueue;

  private String ipAddress;
  private String port;
  private String function;
  private String poisonPill;

  private long totalSuccessfulPostRequests;
  private long totalFailedPostRequests;
  private long totalSuccessfulGetRequests;
  private long totalFailedGetRequests;
  private List<LatencyRecord> postReqLatencyList = new ArrayList<>();
  private List<LatencyRecord> getReqLatencyList = new ArrayList<>();

  private ExecutorService executor;
  private CompletionService<ThreadRecord> postCompletionService, getCompletionService;

  public MultithreadedClient(Integer maxThreads,
      BlockingQueue<String> textInput, String ipAddress, String port, String function,
      String poisonPill) {
    this.maxThreads = maxThreads;
    this.lineQueue = textInput;
    this.ipAddress = ipAddress;
    this.port = port;
    this.function = function;
    this.poisonPill = poisonPill;
    this.executor = Executors.newFixedThreadPool(maxThreads);
    this.postCompletionService = new ExecutorCompletionService<>(this.executor);
    this.getCompletionService = new ExecutorCompletionService<>(this.executor);
  }

  public void start() throws InterruptedException {
    submitThreads();
    updateRequestResults();
    shutdownExecutor();
  }

  private void submitThreads() {
    ThreadInput threadInput = new ThreadInput(this.ipAddress, this.port);
    for (int i = 0; i < maxThreads; i++) {
      PostCallable postCallable = new PostCallable(lineQueue, threadInput, function, poisonPill);
      postCompletionService.submit(postCallable);
    }
    GetCallable getCallable = new GetCallable(threadInput, function);
    getCompletionService.submit(getCallable);
  }

  private void updateRequestResults() {
    try {
      for (int i = 0; i < maxThreads; i++) {
        Future<ThreadRecord> f = postCompletionService.take();
        ThreadRecord record = f.get();
//        System.out.println("future get: " + Thread.currentThread().getId());
        this.totalSuccessfulPostRequests += record.getNSuccessRequest();
        this.totalFailedPostRequests += record.getNFailedRequest();
        postReqLatencyList.addAll(record.getLatencyList());
      }
      Future<ThreadRecord> getReqFuture = getCompletionService.take();
      ThreadRecord getReqRecord = getReqFuture.get();
      this.totalSuccessfulGetRequests += getReqRecord.getNSuccessRequest();
      this.totalFailedGetRequests += getReqRecord.getNFailedRequest();
      getReqLatencyList.addAll(getReqRecord.getLatencyList());
    } catch (InterruptedException e) {
      logger.info("Thread interrupted");
      Thread.currentThread().interrupt();
    } catch (ExecutionException | CancellationException e) {
      logger.info(e.getMessage());
    }
  }

  private void shutdownExecutor() {
    try {
      int tries = 0;
      executor.shutdown();
      while (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        executor.shutdownNow();
        if (++tries > 3) {
          logger.fatal("Unable to shutdown thread executor. Calling System.exit()...");
          System.exit(0);
        }
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  public long getTotalSuccessfulPostRequests() {
    return totalSuccessfulPostRequests;
  }

  public long getTotalFailedPostRequests() {
    return totalFailedPostRequests;
  }

  public List<LatencyRecord> getPostLatencyList() {
    return postReqLatencyList;
  }

  public long getTotalSuccessfulGetRequests() {
    return totalSuccessfulGetRequests;
  }

  public long getTotalFailedGetRequests() {
    return totalFailedGetRequests;
  }

  public List<LatencyRecord> getGetReqLatencyList() {
    return getReqLatencyList;
  }

}

