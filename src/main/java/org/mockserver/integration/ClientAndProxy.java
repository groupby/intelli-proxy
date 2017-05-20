package org.mockserver.integration;

import org.mockserver.client.proxy.ProxyClient;
import org.mockserver.proxy.direct.DirectProxy;

/**
 * @author jamesdbloom
 */
public class ClientAndProxy extends ProxyClient {

    private final DirectProxy httpProxy;

    public ClientAndProxy(Integer port, String remoteHost, Integer remotePort, String cacheLocation) {
        super("localhost", port);
        httpProxy = new DirectProxy(port, remoteHost, remotePort, cacheLocation);
    }

    public boolean isRunning() {
        return httpProxy.isRunning();
    }
}
