package com.groupbyinc.intelliproxy;

import org.junit.rules.ExternalResource;
import org.mockserver.integration.ClientAndProxy;
import org.mockserver.socket.PortFactory;

public class HttpReplayProxy extends ExternalResource {

  private String liveHost;
  private int livePort;
  private int connectedPort;
  private boolean record = false;
  private ClientAndProxy proxy;
  private String cacheDirectory;

  public HttpReplayProxy(String liveHost, int livePort) {
    this(liveHost, livePort, "true".equals(System.getProperty("intelliproxy.record", "false")), "src/test/resources/intelliproxy");
  }
  public HttpReplayProxy(String liveHost, int livePort, boolean record) {
    this(liveHost, livePort, record, "src/test/resources/intelliproxy");
  }

  public HttpReplayProxy(String liveHost, int livePort, boolean record, String cacheDirectory) {
    this.liveHost = liveHost;
    this.livePort = livePort;
    this.record = record;
    this.cacheDirectory = cacheDirectory;
  }

  @Override
  protected void before() throws Throwable {
    super.before();
    connectedPort = PortFactory.findFreePort();
    proxy = new ClientAndProxy(connectedPort, liveHost, livePort, cacheDirectory, record);
  }

  @Override
  protected void after() {
    super.after();
    proxy.stop();
  }

  public int getConnectedPort() {
    return connectedPort;
  }

  public void setCacheDirectory(String cacheDirectory) {
    this.cacheDirectory = cacheDirectory;
  }

  public String getCacheDirectory() {
    return cacheDirectory;
  }

  public boolean isRecord() {
    return record;
  }
}
