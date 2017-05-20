package com.groupbyinc.intelliproxy;

import org.junit.rules.ExternalResource;
import org.mockserver.integration.ClientAndProxy;
import org.mockserver.socket.PortFactory;



/**
 * Created by will on 19/05/17.
 */
public class SimpleReplayProxy extends ExternalResource {

  private static final String DEFAULT_CACHE_DIRECTORY = "src/test/resources/intelliproxy";
  private static final boolean DEFAULT_RECORD = false;
  private String liveHost;
  private int livePort;
  private int connectedPort;
  private boolean record = false;
  private ClientAndProxy proxy;
  private String cacheDirectory;

  public SimpleReplayProxy(String liveHost, int livePort) {
    this(liveHost, livePort, DEFAULT_RECORD, DEFAULT_CACHE_DIRECTORY);
  }

  public SimpleReplayProxy(String liveHost, int livePort, boolean record, String cacheDirectory) {
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
}
