package org.openhab.binding.linktap.internal.rest;

import static org.openhab.binding.linktap.internal.linktapBindingConstants.KEEP_ALIVE_MILLIS;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.openhab.binding.linktap.handler.LinktapRedirectUrlSupplier;
import org.openhab.binding.linktap.internal.LinktapUtils;
import org.openhab.binding.linktap.internal.data.TopLevelData;
import org.openhab.binding.linktap.internal.data.TopLevelStreamingData;
import org.openhab.binding.linktap.internal.exceptions.FailedResolvingLinktapUrlException;
import org.openhab.binding.linktap.internal.listener.linktapStreamingDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class LinktapStreamingRestClient {
    // Assume connection timeout when 2 keep alive message should have been received
    private static final long CONNECTION_TIMEOUT_MILLIS = 2 * KEEP_ALIVE_MILLIS + KEEP_ALIVE_MILLIS / 2;

    public static final String AUTH_REVOKED = "auth_revoked";
    public static final String ERROR = "error";
    public static final String KEEP_ALIVE = "keep-alive";
    public static final String OPEN = "open";
    public static final String PUT = "put";

    private final Logger logger = LoggerFactory.getLogger(LinktapStreamingRestClient.class);

    private final List<linktapStreamingDataListener> listeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler;
    private final Object startStopLock = new Object();

    private String accessToken;
    private @Nullable ScheduledFuture<?> checkConnectionJob;
    private boolean connected;
    private @Nullable EventSource eventSource;
    private long lastEventTimestamp;
    private @Nullable TopLevelData lastReceivedTopLevelData;
    private LinktapRedirectUrlSupplier redirectUrlSupplier;

    public LinktapStreamingRestClient(String accessToken, LinktapRedirectUrlSupplier redirectUrlSupplier,
            ScheduledExecutorService scheduler) {
        this.accessToken = accessToken;
        this.redirectUrlSupplier = redirectUrlSupplier;
        this.scheduler = scheduler;
    }

    private EventSource createEventSource() throws FailedResolvingLinktapUrlException {
        SSLContext sslContext = SslConfigurator.newInstance().createSSLContext();
        Client client = ClientBuilder.newBuilder().sslContext(sslContext).register(SseFeature.class)
                .register(new LinktapStreamingRequestFilter(accessToken)).build();
        EventSource eventSource = new EventSource(client.target(redirectUrlSupplier.getRedirectUrl()), false);
        eventSource.register(this::onEvent);
        return eventSource;
    }

    private void checkConnection() {
        long millisSinceLastEvent = System.currentTimeMillis() - lastEventTimestamp;
        if (millisSinceLastEvent > CONNECTION_TIMEOUT_MILLIS) {
            logger.debug("Check: Disconnected from streaming events, millisSinceLastEvent={}", millisSinceLastEvent);
            synchronized (startStopLock) {
                stopCheckConnectionJob(false);
                if (connected) {
                    connected = false;
                    listeners.forEach(listener -> listener.onDisconnected());
                }
                redirectUrlSupplier.resetCache();
                reopenEventSource();
                startCheckConnectionJob();
            }
        } else {
            logger.debug("Check: Receiving streaming events, millisSinceLastEvent={}", millisSinceLastEvent);
        }
    }

    /**
     * Closes the existing EventSource and opens a new EventSource as workaround when the EventSource fails to reconnect
     * itself.
     */
    private void reopenEventSource() {
        try {
            logger.debug("Reopening EventSource");
            closeEventSource(10, TimeUnit.SECONDS);

            logger.debug("Opening new EventSource");
            EventSource localEventSource = createEventSource();
            localEventSource.open();

            eventSource = localEventSource;
        } catch (FailedResolvingLinktapUrlException e) {
            logger.debug("Failed to resolve Nest redirect URL while opening new EventSource");
        }
    }

    public void start() {
        synchronized (startStopLock) {
            logger.debug("Opening EventSource and starting checkConnection job");
            reopenEventSource();
            startCheckConnectionJob();
            logger.debug("Started");
        }
    }

    public void stop() {
        synchronized (startStopLock) {
            logger.debug("Closing EventSource and stopping checkConnection job");
            stopCheckConnectionJob(true);
            closeEventSource(0, TimeUnit.SECONDS);
            logger.debug("Stopped");
        }
    }

    private void closeEventSource(long timeout, TimeUnit timeoutUnit) {
        EventSource localEventSource = eventSource;
        if (localEventSource != null) {
            if (!localEventSource.isOpen()) {
                logger.debug("Existing EventSource is already closed");
            } else if (localEventSource.close(timeout, timeoutUnit)) {
                logger.debug("Succesfully closed existing EventSource");
            } else {
                logger.debug("Failed to close existing EventSource");
            }
            eventSource = null;
        }
    }

    private void startCheckConnectionJob() {
        ScheduledFuture<?> localCheckConnectionJob = checkConnectionJob;
        if (localCheckConnectionJob == null || localCheckConnectionJob.isCancelled()) {
            checkConnectionJob = scheduler.scheduleWithFixedDelay(this::checkConnection, CONNECTION_TIMEOUT_MILLIS,
                    KEEP_ALIVE_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    private void stopCheckConnectionJob(boolean mayInterruptIfRunning) {
        ScheduledFuture<?> localCheckConnectionJob = checkConnectionJob;
        if (localCheckConnectionJob != null && !localCheckConnectionJob.isCancelled()) {
            localCheckConnectionJob.cancel(mayInterruptIfRunning);
            checkConnectionJob = null;
        }
    }

    public boolean addStreamingDataListener(linktapStreamingDataListener listener) {
        return listeners.add(listener);
    }

    public boolean removeStreamingDataListener(linktapStreamingDataListener listener) {
        return listeners.remove(listener);
    }

    public @Nullable TopLevelData getLastReceivedTopLevelData() {
        return lastReceivedTopLevelData;
    }

    private void onEvent(InboundEvent inboundEvent) {
        try {
            lastEventTimestamp = System.currentTimeMillis();

            String name = inboundEvent.getName();
            String data = inboundEvent.readData();

            logger.debug("Received '{}' event, data: {}", name, data);

            if (!connected) {
                logger.debug("Connected to streaming events");
                connected = true;
                listeners.forEach(listener -> listener.onConnected());
            }

            if (AUTH_REVOKED.equals(name)) {
                logger.debug("API authorization has been revoked for access token: {}", data);
                listeners.forEach(listener -> listener.onAuthorizationRevoked(data));
            } else if (ERROR.equals(name)) {
                logger.warn("Error occurred: {}", data);
                listeners.forEach(listener -> listener.onError(data));
            } else if (KEEP_ALIVE.equals(name)) {
                logger.debug("Received message to keep connection alive");
            } else if (OPEN.equals(name)) {
                logger.debug("Event stream opened");
            } else if (PUT.equals(name)) {
                logger.debug("Data has changed (or initial data sent)");
                TopLevelData topLevelData = LinktapUtils.fromJson(data, TopLevelStreamingData.class).getData();
                lastReceivedTopLevelData = topLevelData;
                listeners.forEach(listener -> listener.onNewTopLevelData(topLevelData));
            } else {
                logger.debug("Received unhandled event with name '{}' and data '{}'", name, data);
            }
        } catch (Exception e) {
            // catch exceptions here otherwise they will be swallowed by the implementation
            logger.warn("An exception occurred while processing the inbound event", e);
        }
    }
}
