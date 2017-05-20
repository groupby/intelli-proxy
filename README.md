Intelli-Proxy
===

The goals of intelli-proxy are 
1. An annotation service to record and replay integration tests.
1. records are stored at `src/test/resources/intelliproxy` by default.
1. A set of modules that know how to populate data into an inline version of those systems.

The record parameter can be omitted and defined by the system property `intelliproxy.record` property.

The cache directory can be omitted and defined by the system property `intelliproxy.directory`

The property `record` is set to `false` by default.

the property `directory` is set to `src/test/resources` by default.


Usage
---

```java

// Proxy's requests and stores them to disk if record is set to true.
@Rule
SimpleReplayProxy("liveHost", "8080", true, "src/test/resources"); // Optional record / location

// Can be used as an annotation that preloads data into a running elasticsearch
@Rule
ElasticsearchReplayProxy(new String[]{ "classpath://globalSetup.yaml", "../elasticData.yaml" });

// Or with raw yaml.
@Rule
ElasticsearchReplayProxy("clusterName: groupby\n" 
         + "records: [{'id': '1', 'title':'title'}]");

```

### Record mode

1. Integration test / Regression Test tries to hit the service.
1. Proxy intercepts and sends the request along to the live service
1. Proxy gets the response and stores it to disk. 
1. Proxy sends the response to the test.

![Recording Mode](src/main/resources/images/proxyRecord.jpg?raw=true "Proxy recording flow")

### Replay mode

1. Integration test / Regression Test tries to hit the service.
1. Proxy intercepts the request and reads it from disk. 
1. Proxy sends the response to the test.

![Recording Mode](src/main/resources/images/proxyReplay.jpg?raw=true "Proxy replay flow")


