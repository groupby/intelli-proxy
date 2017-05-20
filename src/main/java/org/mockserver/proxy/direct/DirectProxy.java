package org.mockserver.proxy.direct;

import com.google.common.util.concurrent.SettableFuture;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.mockserver.filters.RequestLogFilter;
import org.mockserver.filters.RequestResponseLogFilter;
import org.mockserver.proxy.Proxy;
import org.mockserver.stop.StopEventQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;

/**
 * This class should not be constructed directly instead use HttpProxyBuilder to build and configure this class
 *
 * @author jamesdbloom
 * @see org.mockserver.proxy.ProxyBuilder
 */
@SuppressWarnings("unused")
public class DirectProxy implements Proxy {

    private static final Logger logger = LoggerFactory.getLogger(DirectProxy.class);
    // proxy
    private final RequestLogFilter requestLogFilter = new RequestLogFilter();
    private final RequestResponseLogFilter requestResponseLogFilter = new RequestResponseLogFilter();
    private final SettableFuture<String> hasStarted;
    private final SettableFuture<String> stopping = SettableFuture.create();
    // netty
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final boolean record;
    private StopEventQueue stopEventQueue = new StopEventQueue();
    private Channel channel;
    // remote socket
    private InetSocketAddress remoteSocket;
    private String cacheLocation;

    /**
     * Start the instance using the ports provided
     * @param localPort  the local port to expose
     * @param remoteHost the hostname of the remote server to connect to
     * @param remotePort the port of the remote server to connect to
     * @param cacheLocation where the files should be saved.
     * @param record
     */
    public DirectProxy(final Integer localPort, final String remoteHost, final Integer remotePort, String cacheLocation, boolean record) {
        if (localPort == null) {
            throw new IllegalArgumentException("You must specify a local port");
        }
        if (remoteHost == null) {
            throw new IllegalArgumentException("You must specify a remote port");
        }
        if (remotePort == null) {
            throw new IllegalArgumentException("You must specify a remote hostname");
        }
        if (cacheLocation == null) {
            throw new IllegalArgumentException("You must specify a cache location");
        }

        this.record = record;
        this.cacheLocation = cacheLocation;
        hasStarted = SettableFuture.create();

        new Thread(() -> {
            try {
                remoteSocket = new InetSocketAddress(remoteHost, remotePort);
                channel = new ServerBootstrap()
                        .group(bossGroup, workerGroup)
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .channel(NioServerSocketChannel.class)
                        .childOption(ChannelOption.AUTO_READ, true)
                        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                        .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 1024, 32 * 1024))
                        .childHandler(new DirectProxyUnificationHandler())
                        .childAttr(HTTP_PROXY, DirectProxy.this)
                        .childAttr(REMOTE_SOCKET, remoteSocket)
                        .childAttr(REQUEST_LOG_FILTER, requestLogFilter)
                        .childAttr(REQUEST_RESPONSE_LOG_FILTER, requestResponseLogFilter)
                        .bind(localPort)
                        .syncUninterruptibly()
                        .channel();

                logger.info("MockServer proxy started on port: {} connected to remote server: {}", ((InetSocketAddress) channel.localAddress()).getPort(), remoteHost + ":" + remotePort);

                hasStarted.set("STARTED");

                channel.closeFuture().syncUninterruptibly();
            } finally {
                bossGroup.shutdownGracefully().syncUninterruptibly();
                workerGroup.shutdownGracefully().syncUninterruptibly();
            }
        }, "MockServer DirectProxy Thread").start();

        try {
            hasStarted.get();
        } catch (Exception e) {
            logger.warn("Exception while waiting for MockServer proxy to complete starting up", e);
        }
    }

    public Future<?> stop() {
        try {
            channel.close();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            stopEventQueue.stopOthers(this).get();
            stopping.set("stopped");
        } catch (Exception ie) {
            logger.trace("Exception while stopping MockServer proxy", ie);
            stopping.setException(ie);
        }
        return stopping;
    }

    public DirectProxy withStopEventQueue(StopEventQueue stopEventQueue) {
        this.stopEventQueue = stopEventQueue;
        this.stopEventQueue.register(this);
        return this;
    }

    public boolean isRunning() {
        return !bossGroup.isShuttingDown() || !workerGroup.isShuttingDown() || !stopping.isDone();
    }

    public Integer getLocalPort() {
        return ((InetSocketAddress) channel.localAddress()).getPort();
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteSocket;
    }

    public String getCacheLocation() {
        return cacheLocation;
    }

    public void setCacheLocation(String cacheLocation) {
        this.cacheLocation = cacheLocation;
    }

    public boolean isRecord() {
        return record;
    }
}
