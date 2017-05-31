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
import org.junit.contrib.java.lang.system.ClearSystemProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;


public class TcpReplayProxyTest {

  private PseudoServer pseudoServer = new PseudoServer();

  @Rule
  public TcpReplayProxy proxyRecording = new TcpReplayProxy("localhost", pseudoServer.getConnectedPort(), true, "target/intelliproxy");

  @Rule
  public TcpReplayProxy proxyPlayback = new TcpReplayProxy("localhost", pseudoServer.getConnectedPort(), false, "target/intelliproxy");

  @Rule
  public final ClearSystemProperties myPropertyIsCleared = new ClearSystemProperties("intelliproxy.record");


  @Before
  @After
  public void always() throws IOException {
    FileUtils.deleteDirectory(new File("target/intelliproxy"));
  }

  public TcpReplayProxyTest() throws IOException {
    // Required to use a rule that throws an exception in the constructor
  }

  @Test
  public void testProgrammaticOverride() throws IOException {
    HttpReplayProxy test1 = new HttpReplayProxy("localhost", pseudoServer.getConnectedPort(), false, "target/intelliproxy");
    assertEquals(false, test1.isRecord());
    HttpReplayProxy test2 = new HttpReplayProxy("localhost", pseudoServer.getConnectedPort(), true, "target/intelliproxy");
    assertEquals(true, test2.isRecord());

    System.setProperty("intelliproxy.record",  "true");
    HttpReplayProxy test3 = new HttpReplayProxy("localhost", pseudoServer.getConnectedPort(), false, "target/intelliproxy");
    assertEquals(false, test3.isRecord());
    HttpReplayProxy test4 = new HttpReplayProxy("localhost", pseudoServer.getConnectedPort(), true, "target/intelliproxy");
    assertEquals(true, test4.isRecord());


    System.setProperty("intelliproxy.record",  "true");
    HttpReplayProxy test5 = new HttpReplayProxy("localhost", pseudoServer.getConnectedPort());
    assertEquals(true, test5.isRecord());
    HttpReplayProxy test6 = new HttpReplayProxy("localhost", pseudoServer.getConnectedPort());
    assertEquals(true, test6.isRecord());

    System.setProperty("intelliproxy.record",  "false");
    HttpReplayProxy test7 = new HttpReplayProxy("localhost", pseudoServer.getConnectedPort());
    assertEquals(false, test7.isRecord());
    HttpReplayProxy test8 = new HttpReplayProxy("localhost", pseudoServer.getConnectedPort());
    assertEquals(false, test8.isRecord());

  }

  @Test
  public void testWithPseudoServer() throws IOException {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    pseudoServer.setResource(methodName + ".html", methodName);
    String bodyProxy = read("http://localhost:" + proxyRecording.getConnectedPort() + "/" + methodName + ".html");
    String bodyPseudoServer = read("http://localhost:" + pseudoServer.getConnectedPort() + "/" + methodName + ".html");
    assertEquals(methodName, bodyProxy);
    assertEquals(bodyProxy, bodyPseudoServer);
  }

  @Test
  public void recordingCreatesCacheAndHitsLiveService() throws IOException {
    proxyRecording.setCacheDirectory("target/intelliproxy");
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    AtomicInteger hitPseudo = new AtomicInteger();
    pseudoServer.setResource(methodName + ".html", new PseudoServlet() {
      @Override
      public WebResource getGetResponse() throws IOException {
        hitPseudo.incrementAndGet();
        return new WebResource(methodName);
      }
    });
    String bodyProxy = read("http://localhost:" + proxyRecording.getConnectedPort() + "/" + methodName + ".html");
    assertEquals("Did not hit the pseudo server", 1, hitPseudo.get());
    String bodyPseudoServer = read("http://localhost:" + pseudoServer.getConnectedPort() + "/" + methodName + ".html");
    assertEquals("Did not hit the pseudo server", 2, hitPseudo.get());
    assertEquals(bodyProxy, bodyPseudoServer);

    assertTrue("Did not create cache", new File("target/intelliproxy/49fecf90059e89460f9d6e450b17af98").exists());
  }

  @Test
  public void testPlaybackDoesNotHitLiveService() throws IOException {
    proxyPlayback.setCacheDirectory("target/intelliproxy");
    //noinspection ResultOfMethodCallIgnored
    new File("target/intelliproxy").mkdirs();
    String response = "{\n" +
        "  \"statusCode\" : 200,\n" +
        "  \"headers\" : [ {\n" +
        "    \"name\" : \"Content-Length\",\n" +
        "    \"values\" : [ \"33\" ]\n" +
        "  }, {\n" +
        "    \"name\" : \"Content-Type\",\n" +
        "    \"values\" : [ \"text/html\" ]\n" +
        "  } ],\n" +
        "  \"body\" : \"testPlaybackDoesNotHitLiveService\"\n" +
        "}";
    FileUtils.write(new File("target/intelliproxy/17816f65161b804f69639478c0974d88"), response, "UTF-8");
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    AtomicInteger hitPseudo = new AtomicInteger();
    pseudoServer.setResource(methodName + ".html", new PseudoServlet() {
      @Override
      public WebResource getGetResponse() throws IOException {
        hitPseudo.incrementAndGet();
        return new WebResource(methodName);
      }
    });

    String bodyProxy = read("http://localhost:" + proxyPlayback.getConnectedPort() + "/" + methodName + ".html");
    assertEquals("Hit the pseudo server", 0, hitPseudo.get());
    assertEquals(methodName, bodyProxy);
  }

  @Test
  public void testMissingResourceCausesException() throws IOException {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    try {
      read("http://localhost:" + proxyPlayback.getConnectedPort() + "/" + methodName + ".html");
      fail("Should throw exception for nonexistent url");
    } catch (FileNotFoundException e) {
      // expected
    }
  }

  private String read(String urlLocation) throws IOException {
    InputStream in = new URL(urlLocation).openConnection().getInputStream();
    String body = IOUtils.toString(in, "UTF-8");
    IOUtils.closeQuietly(in);
    return body;
  }

}