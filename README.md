Intelli-Proxy
===


Service to record and replay integration tests.

Modules that know how to populate data into the source systems.

Record mode
---

1. Integration test / Regression Test tries to hit the service.
2. Proxy intercepts and sends the request along to the live service
3. Proxy gets the response and stores it to disk. 
4. Proxy sends the response to the test.

![Recording Mode](src/main/resources/images/proxyRecord.jpg?raw=true "Proxy recording flow")

Replay mode
---

1. Integration test / Regression Test tries to hit the service.
2. Proxy intercepts the request and reads it from disk. 
3. Proxy sends the response to the test.

![Recording Mode](src/main/resources/images/proxyReplay.jpg?raw=true "Proxy replay flow")


Usage
---

```java

// Proxy's requests and stores them to disk if record is set to true.
@DumbReplayProxy(record = false, localPort = 9200, liveHost = "somehost", livePort = "8080");

// Can be used as an annotation that preloads data into a running elasticsearch
@ElasticSearchReplayProxy(record = false, port = 9200, data = "../elasticData.yaml");

// Or with raw yaml.
@ElasticSearchReplayProxy(record = false, port = 9200, 
    yaml = "clusterName: groupby\n" 
           + "records=[{'id': '1', 'title':'title'}]");

```