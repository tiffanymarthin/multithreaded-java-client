package neu.cs6650.model;

import java.util.LinkedList;
import java.util.List;

public class ThreadRecord {
  private int nSuccessRequest;
  private int nFailedRequest;
  private List<LatencyRecord> latencyList;

  public ThreadRecord(int nSuccessRequest, int nFailedRequest, List<LatencyRecord> latencyList) {
    this.nSuccessRequest = nSuccessRequest;
    this.nFailedRequest = nFailedRequest;
    this.latencyList = latencyList;
  }

  public int getNSuccessRequest() {
    return nSuccessRequest;
  }

  public int getNFailedRequest() {
    return nFailedRequest;
  }

  public List<LatencyRecord> getLatencyList() {
    return latencyList;
  }
}
