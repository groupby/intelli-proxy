package com.groupbyinc.intelliproxy;

import com.github.terma.javaniotcpproxy.TcpProxy;
import com.github.terma.javaniotcpproxy.TcpProxyConfig;
import org.junit.rules.ExternalResource;
import org.mockserver.integration.ClientAndProxy;
import org.mockserver.socket.PortFactory;

public class TcpReplayProxy extends ExternalResource {

  private String liveHost;
  private int livePort;
  private int connectedPort;
  private boolean record = false;
//  private TcpProxy proxy;
  private SimpleProxyServer proxy;
  private String cacheDirectory;

  public TcpReplayProxy(String liveHost, int livePort) {
    this(liveHost, livePort, "true".equals(System.getProperty("intelliproxy.record", "false")), "src/test/resources/intelliproxy");
  }
  public TcpReplayProxy(String liveHost, int livePort, boolean record) {
    this(liveHost, livePort, record, "src/test/resources/intelliproxy");
  }

  public TcpReplayProxy(String liveHost, int livePort, boolean record, String cacheDirectory) {
    this.liveHost = liveHost;
    this.livePort = livePort;
    this.record = record;
    this.cacheDirectory = cacheDirectory;
  }

  @Override
  protected void before() throws Throwable {
    super.before();
    connectedPort = PortFactory.findFreePort();
    TcpProxyConfig config = new TcpProxyConfig(connectedPort, liveHost, livePort);
    config.setWorkerCount(10);
     proxy = new SimpleProxyServer(liveHost, livePort, connectedPort);
  proxy.start();
  }

  @Override
  protected void after() {
    super.after();
    proxy.shutdown();
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
