package com.groupbyinc.intelliproxy;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;

import com.meterware.pseudoserver.PseudoServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;


/**
 * Created by will on 19/05/17.
 */
public class SimpleReplayProxyTest {


  private PseudoServer pseudoServer = new PseudoServer();

  @Rule
  public SimpleReplayProxy proxy = new SimpleReplayProxy("localhost", pseudoServer.getConnectedPort());

  public SimpleReplayProxyTest() throws IOException {
  }

  @Test
  public void testWithPseudoServer() throws IOException {
    pseudoServer.setResource("test.html", "body");
    System.out.println(proxy.getConnectedPort());
    String bodyProxy = read("http://localhost:" + proxy.getConnectedPort() + "/test.html");
    String bodyPseudoServer = read("http://localhost:" + pseudoServer.getConnectedPort() + "/test.html");
    assertEquals("body", bodyProxy);
    assertEquals(bodyProxy, bodyPseudoServer);
  }

  private String read(String urlLocation) throws IOException {
    InputStream in = new URL(urlLocation).openConnection().getInputStream();
    String body = IOUtils.toString(in, "UTF-8");
    in.close();
    return body;
  }

}