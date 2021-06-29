package neu.cs6650.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import neu.cs6650.Configuration;
import neu.cs6650.model.LatencyRecord;
import neu.cs6650.model.ThreadInput;
import neu.cs6650.model.ThreadRecord;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PostCallable implements Callable<ThreadRecord> {

  private static final Logger logger = LogManager.getLogger(PostCallable.class.getName());

  private BlockingQueue<String> lineQueue;
  private ThreadInput threadInput;
  private String apiRoute;
  private String function;
  private String poisonPill;
  private List<LatencyRecord> latencyList;

  private final static String WEB_APP = "java-servlet";
//  private final static String WEB_APP = Configuration.IS_LOCAL ? "java_servlet_rmq" : "java-servlet";
  private final static String API_PATH = "textbody";
  private final static String CONTENT_TYPE = "application/json; charset=utf-8";


  private final OkHttpClient client = new OkHttpClient.Builder()
      .connectTimeout(40, TimeUnit.SECONDS)
      .readTimeout(5, TimeUnit.SECONDS)
      .build();

  public PostCallable(BlockingQueue<String> lineQueue, ThreadInput threadInput, String function,
      String poisonPill) {
    this.lineQueue = lineQueue;
    this.threadInput = threadInput;
    this.apiRoute =
        "http://" + this.threadInput.getIpAddress() + ":" + this.threadInput.getPort() + "/"
            + WEB_APP;
    this.function = function;
    this.poisonPill = poisonPill;
    this.latencyList = new ArrayList<>();
  }

  @Override
  public ThreadRecord call() {
    int totalSuccessCall = 0, totalFailedCall = 0;
    String localVarPostBody = null;

    // create path and map variables
    final String localVarPath = "/" + API_PATH + "/" + this.function + "/";

    while (true) {
      try {
        localVarPostBody = this.lineQueue.take();
//        System.out.println("c: " + localVarPostBody);
        if (localVarPostBody.equals(this.poisonPill)) {
          return new ThreadRecord(totalSuccessCall, totalFailedCall, latencyList);
        } else {
          if (postRequest(localVarPath, localVarPostBody, CONTENT_TYPE)) {
            totalSuccessCall++;
          } else {
            totalFailedCall++;
          }
        }
      } catch (InterruptedException e) {
        logger.info("Thread interrupted");
        Thread.currentThread().interrupt();
      }
    }
//    return new ThreadRecord(totalSuccessCall, totalFailedCall);
  }

  public boolean postRequest(String path, String postBody, String contentType) {
    long startTime, endTime;
//    startTime = System.currentTimeMillis();
    Request request = buildPostCall(path, postBody, contentType);
    startTime = System.currentTimeMillis();
    try (Response response = client.newCall(request).execute()) {
      endTime = System.currentTimeMillis();
      LatencyRecord latencyRecord = new LatencyRecord(startTime, "POST", endTime - startTime, response.code());
      latencyList.add(latencyRecord);
      return response.code() == 200;
    } catch (IOException e) {
      logger.info(e.getMessage());
      return false;
    }
  }

  private Request buildPostCall(String path, String postBody, String contentType) {
    String url = buildUrl(path);
    RequestBody reqBody = RequestBody.create(postBody, MediaType.parse(contentType));
    return new Builder()
        .url(url)
        .post(reqBody)
        .build();
  }

  private String buildUrl(String path) {
    return this.apiRoute + path;
  }

}