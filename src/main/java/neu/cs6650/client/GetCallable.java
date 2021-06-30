package neu.cs6650.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import neu.cs6650.Configuration;
import neu.cs6650.model.LatencyRecord;
import neu.cs6650.model.ThreadInput;
import neu.cs6650.model.ThreadRecord;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetCallable implements Callable<ThreadRecord> {

  private static final Logger logger = LogManager
      .getLogger(neu.cs6650.client.GetCallable.class.getName());

  private ThreadInput threadInput;
  private String apiRoute;
  private String function;
  private List<LatencyRecord> latencyList;

  private final static String WEB_APP = "java-servlet";
  private final static String API_PATH = "textbody";
  private final static String CONTENT_TYPE = "application/json; charset=utf-8";
  private final static List<String> WORD_LIST = new ArrayList<>
      (Arrays.asList("the", "and", "because", "system", "computer",
          "distributed", "any", "is", "servlet", "performance"));

  private final OkHttpClient client = new OkHttpClient.Builder()
      .connectTimeout(Configuration.CONNECT_TIMEOUT, TimeUnit.SECONDS)
      .readTimeout(Configuration.READ_TIMEOUT, TimeUnit.SECONDS)
      .build();

  public GetCallable(ThreadInput threadInput, String function) {
    this.threadInput = threadInput;
    this.apiRoute =
        "http://" + this.threadInput.getIpAddress() + ":" + this.threadInput.getPort() + "/"
            + WEB_APP;
    this.function = function;
    this.latencyList = new ArrayList<>();
  }

  @Override
  public ThreadRecord call() throws InterruptedException {
    Thread.currentThread().sleep(1000);
    int totalSuccessCall = 0, totalFailedCall = 0;

    // create path and map variables
    final String localVarPath = "/" + API_PATH + "/" + this.function + "/";
    for (String word : WORD_LIST) {
      String path = localVarPath + word + "/";
      if (getRequest(path)) {
        totalSuccessCall++;
      } else {
        totalFailedCall++;
      }
    }
    return new ThreadRecord(totalSuccessCall, totalFailedCall, latencyList);
  }

  public boolean getRequest(String path) {
    long startTime, endTime;
    Request request = buildGetCall(path);
    startTime = System.currentTimeMillis();
    try (Response response = client.newCall(request).execute()) {
      endTime = System.currentTimeMillis();
      LatencyRecord latencyRecord = new LatencyRecord(startTime, "GET", endTime - startTime,
          response.code());
      latencyList.add(latencyRecord);
      logger.info(response.body().string() + "\n");
      return response.code() == 200;
    } catch (IOException e) {
      logger.info(e.getMessage());
      return false;
    }
  }

  private Request buildGetCall(String path) {
    String url = buildUrl(path);
    return new Builder()
        .url(url)
        .build();
  }

  private String buildUrl(String path) {
    return this.apiRoute + path;
  }
}
