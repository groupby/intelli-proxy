Intelli-Proxy
===


Service to record and replay integration tests.

Modules that know how to populate data into the source systems.

<img scr="src/main/resources/images/proxyRecord.jpg">
<img scr="src/main/resources/images/proxyReplay.jpg">


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