package com.groupbyinc.intelliproxy;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.mockserver.client.serialization.HttpResponseSerializer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.io.File;
import java.io.IOException;

public class FileBackedCache {
  public void save(HttpRequest request, HttpResponse response, String location) throws IllegalStateException {
    String filename = getFilename(request);
    System.out.println(filename);
    if (!new File(location).exists()) {
      //noinspection ResultOfMethodCallIgnored
      new File(location).mkdirs();
    }

    try {
      FileUtils.write(new File(location + "/" + filename), response.toString(), "UTF-8");
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

  }

  private String getFilename(HttpRequest request) {
    String host = request.getFirstHeader("Host");
    if (host != null) {
      request.replaceHeader(new Header("Host", "nulledForCacheCreation"));
    }
    String digest = DigestUtils.md5Hex(request.toString());
    if (host != null) {
      request.replaceHeader(new Header("Host", host));
    }
    return digest;

  }

  /**
   * Hash the request and load a response from disk or null if nothing can be found.
   * @param request the http request
   * @param location the location of the cache
   * @return a response object constructed from the contents of the file.
   * @throws IllegalStateException If anything disk-y happens.
   */
  public HttpResponse load(HttpRequest request, String location) throws IllegalStateException {
    try {
      String filename = getFilename(request);
      File file = new File(location + "/" + filename);
      if (file.exists()) {
        return new HttpResponseSerializer().deserialize(FileUtils.readFileToString(file, "UTF-8"));
      } else {
        return null;
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
