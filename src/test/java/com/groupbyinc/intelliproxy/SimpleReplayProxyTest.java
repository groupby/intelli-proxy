package com.groupbyinc.intelliproxy;

import com.meterware.pseudoserver.PseudoServer;
import com.meterware.pseudoserver.PseudoServlet;
import com.meterware.pseudoserver.WebResource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SimpleReplayProxyTest {

  private PseudoServer pseudoServer = new PseudoServer();

  @Rule
  public SimpleReplayProxy proxy = new SimpleReplayProxy("localhost", pseudoServer.getConnectedPort(), true, "target/intelliproxy");

  @Before
  @After
  public void always() throws IOException {
    FileUtils.deleteDirectory(new File("target/intelliproxy"));
  }

  public SimpleReplayProxyTest() throws IOException {
    // Required to use a rule that throws an exception in the constructor
  }

  @Test
  public void testWithPseudoServer() throws IOException {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    pseudoServer.setResource(methodName + ".html", methodName);
    String bodyProxy = read("http://localhost:" + proxy.getConnectedPort() + "/" + methodName + ".html");
    String bodyPseudoServer = read("http://localhost:" + pseudoServer.getConnectedPort() + "/" + methodName + ".html");
    assertEquals(methodName, bodyProxy);
    assertEquals(bodyProxy, bodyPseudoServer);
  }

  @Test
  public void recordingCreatesCacheAndHitsLiveService() throws IOException {
    proxy.setCacheDirectory("target/intelliproxy");
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    AtomicInteger hitPseudo = new AtomicInteger();
    pseudoServer.setResource(methodName + ".html", new PseudoServlet() {
      @Override
      public WebResource getGetResponse() throws IOException {
        hitPseudo.incrementAndGet();
        return new WebResource(methodName);
      }
    });
    String bodyProxy = read("http://localhost:" + proxy.getConnectedPort() + "/" + methodName + ".html");
    assertEquals("Did not hit the pseudo server", 1, hitPseudo.get());
    String bodyPseudoServer = read("http://localhost:" + pseudoServer.getConnectedPort() + "/" + methodName + ".html");
    assertEquals("Did not hit the pseudo server", 2, hitPseudo.get());
    assertEquals(bodyProxy, bodyPseudoServer);

    assertTrue("Did not create cache", new File("target/intelliproxy/49fecf90059e89460f9d6e450b17af98").exists());

  }


  private String read(String urlLocation) throws IOException {
    InputStream in = new URL(urlLocation).openConnection().getInputStream();
    String body = IOUtils.toString(in, "UTF-8");
    IOUtils.closeQuietly(in);
    return body;
  }

}