package org.mockserver.integration;

import org.mockserver.client.proxy.ProxyClient;
import org.mockserver.proxy.direct.DirectProxy;

/**
 * @author jamesdbloom
 */
public class ClientAndProxy extends ProxyClient {

    private final DirectProxy httpProxy;

    public ClientAndProxy(Integer port, String remoteHost, Integer remotePort, String cacheLocation, boolean record) {
        super("localhost", port);
        httpProxy = new DirectProxy(port, remoteHost, remotePort, cacheLocation, record);
    }

    public boolean isRunning() {
        return httpProxy.isRunning();
    }
}
