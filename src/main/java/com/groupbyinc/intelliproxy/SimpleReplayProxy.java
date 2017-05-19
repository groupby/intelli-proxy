package com.groupbyinc.intelliproxy;

import com.github.terma.javaniotcpproxy.TcpProxy;
import com.github.terma.javaniotcpproxy.TcpProxyConfig;
import org.junit.rules.ExternalResource;

import java.net.ServerSocket;

/**
 * Created by will on 19/05/17.
 */
public class SimpleReplayProxy extends ExternalResource {

  private String liveHost;
  private int livePort;
  private int connectedPort;
  private TcpProxy proxy = null;

  public SimpleReplayProxy(String liveHost, int livePort) {
    this.liveHost = liveHost;
    this.livePort = livePort;
  }

  @Override
  protected void before() throws Throwable {
    super.before();
    ServerSocket socket = new ServerSocket(0);
    connectedPort = socket.getLocalPort();
    socket.close();
    TcpProxyConfig config = new TcpProxyConfig(connectedPort, liveHost, livePort);
    config.setWorkerCount(10);
    proxy = new TcpProxy(config);
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
}
