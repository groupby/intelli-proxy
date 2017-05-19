Intelli-Proxy
===

Service to record and replay integration tests.

Modules that know how to populate data into the source systems.


Usage
---

```java

// store all the TCP requests, stores them as binary if record is set to true, replay's 
// them if record is set to false.
@DumbReplayProxy(record = false, localPort = 9200, liveHost = "somehost", livePort = "8080");

// Can be used as an annotation that preloads data into a running elasticsearch
@ElasticSearchReplayProxy(record = false, port = 9200, data = "../elasticData.yaml");

// Or with raw yaml.
@ElasticSearchReplayProxy(record = false, port = 9200, 
    yaml = "clusterName: groupby\n" 
           + "records=[{'id': '1', 'title':'title'}]");

```