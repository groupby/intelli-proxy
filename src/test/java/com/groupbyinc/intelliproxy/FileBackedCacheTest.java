package com.groupbyinc.intelliproxy;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FileBackedCacheTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void testSave() throws IOException {
    String location = temporaryFolder.newFolder().getPath();
    FileBackedCache test = new FileBackedCache();
    HttpRequest request = new HttpRequest().withPath("testSave.html").withHeader("Host", "localhost:" + Math.random());
    HttpResponse response = new HttpResponse().withBody("content").withStatusCode(200).withCookie("bob", "the builder");
    test.save(request, response, location);
    assertEquals("{\n" +
        "  \"statusCode\" : 200,\n" +
        "  \"cookies\" : [ {\n" +
        "    \"name\" : \"bob\",\n" +
        "    \"value\" : \"the builder\"\n" +
        "  } ],\n" +
        "  \"body\" : \"content\"\n" +
        "}", FileUtils.readFileToString(new File(location + "/37bd99c8babab134208d9a4e71ef434f"), "UTF-8"));
  }

  @Test
  public void testLoad() throws IOException {
    String location = temporaryFolder.newFolder().getPath();
    FileBackedCache test = new FileBackedCache();
    HttpRequest request = new HttpRequest().withPath("testLoad.html");
    HttpResponse response = new HttpResponse().withBody("content").withStatusCode(200).withCookie("bob", "the builder");
    test.save(request, response, location);
    assertEquals("{\n" +
        "  \"statusCode\" : 200,\n" +
        "  \"cookies\" : [ {\n" +
        "    \"name\" : \"bob\",\n" +
        "    \"value\" : \"the builder\"\n" +
        "  } ],\n" +
        "  \"body\" : \"content\"\n" +
        "}", test.load(request, location).toString());
  }

  @Test
  public void testNoContent() throws IOException {
    String location = temporaryFolder.newFolder().getPath();
    FileBackedCache test = new FileBackedCache();
    HttpRequest request = new HttpRequest().withPath("testNoContent.html");
    assertNull(test.load(request, location));
  }


}