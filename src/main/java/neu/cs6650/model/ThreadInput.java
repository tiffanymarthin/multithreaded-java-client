package neu.cs6650.model;

public class ThreadInput {

  private String ipAddress;
  private String port;
//  private Integer startTime;
//  private Integer endTime;
//  private Integer numOfTask;


  public ThreadInput(String ipAddress, String port) {
    this.ipAddress = ipAddress;
    this.port = port;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getPort() {
    return port;
  }
}