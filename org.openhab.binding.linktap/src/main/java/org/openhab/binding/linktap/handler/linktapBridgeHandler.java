/**
 * /**
 * * Copyright (c) 2010-2019 Contributors to the openHAB project
 * *
 * * See the NOTICE file(s) distributed with this work for additional
 * * information.
 * *
 * * This program and the accompanying materials are made available under the
 * * terms of the Eclipse Public License 2.0 which is available at
 * * http://www.eclipse.org/legal/epl-2.0
 * *
 * * SPDX-License-Identifier: EPL-2.0
 * *
 * * @author Timmy Becker - Initial contribution
 */

package org.openhab.binding.linktap.handler;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openhab.binding.linktap.internal.linktapBindingConstants.REST_CONTENT_TYPE_PARAM;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.linktap.internal.LinktapUtils;
import org.openhab.binding.linktap.internal.config.LinktapBridgeConfiguration;
import org.openhab.binding.linktap.internal.data.ErrorData;
import org.openhab.binding.linktap.internal.data.TopLevelData;
import org.openhab.binding.linktap.internal.data.linktapIdentifiable;
import org.openhab.binding.linktap.internal.exceptions.FailedResolvingLinktapUrlException;
import org.openhab.binding.linktap.internal.exceptions.FailedSendingLinktapDataException;
import org.openhab.binding.linktap.internal.exceptions.InvalidConnectionParameters;
import org.openhab.binding.linktap.internal.listener.linktapStreamingDataListener;
import org.openhab.binding.linktap.internal.listener.linktapThingDataListener;
import org.openhab.binding.linktap.internal.rest.LinktapConnectionTest;
import org.openhab.binding.linktap.internal.rest.LinktapStreamingRestClient;
import org.openhab.binding.linktap.internal.rest.LinktapUpdateRequest;
import org.openhab.binding.linktap.internal.update.LinktapCompositeUpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LinkTapHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Timmy Becker - Initial contribution
 */

@NonNullByDefault
public class linktapBridgeHandler extends BaseBridgeHandler implements linktapStreamingDataListener {

    private final Logger logger = LoggerFactory.getLogger(linktapBridgeHandler.class);
    private static final int REQUEST_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(30);

    // private @Nullable linktapConfiguration config;

    private final List<LinktapUpdateRequest> LinktapUpdateRequests = new CopyOnWriteArrayList<>();
    private final LinktapCompositeUpdateHandler updateHandler = new LinktapCompositeUpdateHandler(
            this::getPresentThingsLinktapIds);

    private @NonNullByDefault({}) LinktapConnectionTest authorizer;
    private @NonNullByDefault({}) LinktapBridgeConfiguration config;

    private @Nullable ScheduledFuture<?> initializeJob;
    private @Nullable ScheduledFuture<?> transmitJob;
    private @Nullable LinktapRedirectUrlSupplier redirectUrlSupplier;
    private @Nullable LinktapStreamingRestClient streamingRestClient;

    /**
     * Creates the bridge handler to connect toLinktap.
     *
     * @param bridge The bridge to connect to Linktap with.
     */
    public linktapBridgeHandler(Bridge gateway) {
        super(gateway);

    }

    /**
     * Initialize the connection to Nest.
     */

    @Override
    public void initialize() {

        // String accessToken; // TODO remove

        logger.debug("Initializing Linktap bridge handler");
        config = getConfigAs(LinktapBridgeConfiguration.class);
        authorizer = new LinktapConnectionTest(config);
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Starting poll query");

        /*
         * try {
         * accessToken = authorizer.getUsername();
         * } catch (InvalidConnectionParameters e1) {
         * // TODO Auto-generated catch block
         * e1.printStackTrace();
         * }
         */

        initializeJob = scheduler.schedule(() -> {
            try {
                logger.debug("Product ID      {}", config.gatewayId);
                logger.debug("GW Username  {}", config.username);
                logger.debug("apiKey         {}", config.apiKey);
                logger.debug("Access Token {}", getExistingOrNewAccessToken());
                redirectUrlSupplier = createRedirectUrlSupplier();
                restartStreamingUpdates();
            } catch (InvalidConnectionParameters e) {
                logger.debug("Invalid access token", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Token is invalid and could not be refreshed: " + e.getMessage());
            }
        }, 0, TimeUnit.SECONDS);

        logger.debug("Finished initializing Linktap bridge handler");

        /*
         * TODO test
         *
         */

        // scheduler.execute(this::configure);
    }

    /**
     * Clean up the handler.
     */

    @Override
    public void dispose() {
        logger.debug("Linktap bridge disposed");
        // stopStreamingUpdates();

        ScheduledFuture<?> localInitializeJob = initializeJob;
        if (localInitializeJob != null && !localInitializeJob.isCancelled()) {
            localInitializeJob.cancel(true);
            initializeJob = null;
        }

        ScheduledFuture<?> localTransmitJob = transmitJob;
        if (localTransmitJob != null && !localTransmitJob.isCancelled()) {
            localTransmitJob.cancel(true);
            transmitJob = null;
        }

        this.authorizer = null;
        this.redirectUrlSupplier = null;
        this.streamingRestClient = null;
    }

    public <T> boolean addThingDataListener(Class<T> dataClass, linktapThingDataListener<T> listener) {
        return updateHandler.addListener(dataClass, listener);
    }

    public <T> boolean addThingDataListener(Class<T> dataClass, String nestId, linktapThingDataListener<T> listener) {
        return updateHandler.addListener(dataClass, nestId, listener);
    }

    /**
     * Adds the update request into the queue for doing something with, send immediately if the queue is empty.
     */

    public void addUpdateRequest(LinktapUpdateRequest request) {
        LinktapUpdateRequests.add(request);
        scheduleTransmitJobForPendingRequests();
    }

    /*
     * private void configure() {
     * logger.debug("Update status to ONLINE.");
     *
     * updateStatus(ThingStatus.ONLINE);
     * }
     */

    protected LinktapRedirectUrlSupplier createRedirectUrlSupplier() throws InvalidConnectionParameters {
        return new LinktapRedirectUrlSupplier(getHttpHeaders());
    }

    /*
     * Access Token not required for linktap:
     * private String getExistingOrNewAccessToken() throws InvalidAccessTokenException {
     * String accessToken = config.accessToken;
     * if (accessToken == null || accessToken.isEmpty()) {
     * accessToken = authorizer.getNewAccessToken();
     * config.accessToken = accessToken;
     * config.pincode = "";
     * // Update and save the access token in the bridge configuration
     * Configuration configuration = editConfiguration();
     * // configuration.put(linktapBridgeConfiguration.ACCESS_TOKEN, config.accessToken);
     * // configuration.put(linktapBridgeConfiguration.PINCODE, config.pincode);
     * updateConfiguration(configuration);
     * logger.debug("Retrieved new access token: {}", config.accessToken);
     * return accessToken;
     * } else {
     * logger.debug("Re-using access token from configuration: {}", accessToken);
     * return accessToken;
     * }
     * }
     */

    private String getExistingOrNewAccessToken() throws InvalidConnectionParameters {
        String accessToken = config.apiKey;
        if (accessToken == null || accessToken.isEmpty()) {
            accessToken = authorizer.getApiKey();
            Configuration configuration = editConfiguration();
            configuration.put(LinktapBridgeConfiguration.API_KEY, config.apiKey);
            updateConfiguration(configuration);
            return accessToken;
        } else {
            return accessToken;

        }
    }

    protected Properties getHttpHeaders() throws InvalidConnectionParameters {
        Properties httpHeaders = new Properties();
        // httpHeaders.put("Authorization", "Bearer " + getExistingOrNewAccessToken());
        httpHeaders.put("Content-Type", REST_CONTENT_TYPE_PARAM);
        return httpHeaders;
    }

    public @Nullable <T> T getLastUpdate(Class<T> dataClass, String linktapId) {
        return updateHandler.getLastUpdate(dataClass, linktapId);
    }

    public <T> List<T> getLastUpdates(Class<T> dataClass) {
        return updateHandler.getLastUpdates(dataClass);
    }

    private LinktapRedirectUrlSupplier getOrCreateRedirectUrlSupplier() throws InvalidConnectionParameters {
        LinktapRedirectUrlSupplier localRedirectUrlSupplier = redirectUrlSupplier;
        if (localRedirectUrlSupplier == null) {
            localRedirectUrlSupplier = createRedirectUrlSupplier();
            redirectUrlSupplier = localRedirectUrlSupplier;
        }
        return localRedirectUrlSupplier;
    }

    private Set<String> getPresentThingsLinktapIds() {
        Set<String> linktapIds = new HashSet<>();
        for (Thing thing : getThing().getThings()) {
            ThingHandler handler = thing.getHandler();
            if (handler != null && thing.getStatusInfo().getStatusDetail() != ThingStatusDetail.GONE) {
                linktapIds.add(((linktapIdentifiable) handler).getId());
            }
        }
        return linktapIds;
    }

    /*
     * Handles an incoming command update
     */

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // do nothing
        if (command instanceof RefreshType) {
            logger.debug("Refresh command received");
            updateHandler.resendLastUpdates();
        }
    }

    private void jsonToPutUrl(LinktapUpdateRequest request)
            throws FailedSendingLinktapDataException, InvalidConnectionParameters, FailedResolvingLinktapUrlException {
        try {
            LinktapRedirectUrlSupplier localRedirectUrlSupplier = redirectUrlSupplier;
            if (localRedirectUrlSupplier == null) {
                throw new FailedResolvingLinktapUrlException("redirectUrlSupplier is null");
            }

            String url = localRedirectUrlSupplier.getRedirectUrl() + request.getUpdatePath();
            logger.debug("Putting data to: {}", url);

            String jsonContent = LinktapUtils.toJson(request.getValues());
            logger.debug("PUT content: {}", jsonContent);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
            String jsonResponse = HttpUtil.executeUrl("PUT", url, getHttpHeaders(), inputStream,
                    REST_CONTENT_TYPE_PARAM, REQUEST_TIMEOUT);
            logger.debug("PUT response: {}", jsonResponse);

            ErrorData error = LinktapUtils.fromJson(jsonResponse, ErrorData.class);
            if (StringUtils.isNotBlank(error.getError())) {
                logger.debug("Linktap API error: {}", error);
                logger.warn("Linktap API error: {}", error.getMessage());
            }
        } catch (IOException e) {
            throw new FailedSendingLinktapDataException("Failed to send data", e);
        }
    }

    @Override
    public void onAuthorizationRevoked(String token) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "Authorization token revoked: " + token);
    }

    // @Override
    @Override
    public void onConnected() {
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Streaming data connection established");
        scheduleTransmitJobForPendingRequests();
    }

    // @Override
    @Override
    public void onDisconnected() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Streaming data disconnected");
    }

    // @Override
    @Override
    public void onError(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
    }

    // @Override
    @Override
    public void onNewTopLevelData(TopLevelData data) {
        updateHandler.handleUpdate(data);
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Receiving streaming data");
    }

    public <T> boolean removeThingDataListener(Class<T> dataClass, linktapThingDataListener<T> listener) {
        return updateHandler.removeListener(dataClass, listener);
    }

    public <T> boolean removeThingDataListener(Class<T> dataClass, String linktapId,
            linktapThingDataListener<T> listener) {
        return updateHandler.removeListener(dataClass, linktapId, listener);
    }

    private void restartStreamingUpdates() {
        synchronized (this) {
            stopStreamingUpdates();
            startStreamingUpdates();
        }
    }

    private void scheduleTransmitJobForPendingRequests() {
        ScheduledFuture<?> localTransmitJob = transmitJob;
        if (!LinktapUpdateRequests.isEmpty() && (localTransmitJob == null || localTransmitJob.isDone())) {
            transmitJob = scheduler.schedule(this::transmitQueue, 0, SECONDS);
        }
    }

    /*
     * private void startStreamingUpdates() {
     * synchronized (this) {
     * try {
     * LinktapStreamingRestClient localStreamingRestClient = new LinktapStreamingRestClient(
     * getExistingOrNewAccessToken(), getOrCreateRedirectUrlSupplier(), scheduler);
     * localStreamingRestClient.addStreamingDataListener(this);
     * localStreamingRestClient.start();
     *
     * streamingRestClient = localStreamingRestClient;
     * } catch (InvalidConnectionParameters e) {
     * logger.debug("Invalid access token", e);
     * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
     * "Token is invalid and could not be refreshed: " + e.getMessage());
     * }
     * }
     * }
     */
    private void startStreamingUpdates() {
        synchronized (this) {
            try {
                LinktapStreamingRestClient localStreamingRestClient = new LinktapStreamingRestClient(
                        getExistingOrNewAccessToken(), getOrCreateRedirectUrlSupplier(), scheduler);
                localStreamingRestClient.addStreamingDataListener(this);
                localStreamingRestClient.start();

                streamingRestClient = localStreamingRestClient;
            } catch (InvalidConnectionParameters e) {
                logger.debug("Invalid access token", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Token is invalid and could not be refreshed: " + e.getMessage());
            }
        }
    }

    private void stopStreamingUpdates() {
        LinktapStreamingRestClient localStreamingRestClient = streamingRestClient;
        if (localStreamingRestClient != null) {
            synchronized (this) {
                localStreamingRestClient.stop();
                localStreamingRestClient.removeStreamingDataListener(this);
                streamingRestClient = null;
            }
        }

    }

    /*
     * TODO activate
     * private void startStreamingUpdates() {
     * synchronized (this) {
     * try {
     * LinktapStreamingRestClient localStreamingRestClient = new LinktapStreamingRestClient(
     * getThing(), getOrCreateRedirectUrlSupplier(), scheduler);
     * localStreamingRestClient.addStreamingDataListener(this);
     * localStreamingRestClient.start();
     *
     * streamingRestClient = localStreamingRestClient;
     * } catch (InvalidAccessTokenException e) {
     * logger.debug("Invalid access token", e);
     * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
     * "Token is invalid and could not be refreshed: " + e.getMessage());
     * }
     * }
     * }
     *
     * private void stopStreamingUpdates() {
     * LinktapStreamingRestClient localStreamingRestClient = streamingRestClient;
     * if (localStreamingRestClient != null) {
     * synchronized (this) {
     * localStreamingRestClient.stop();
     * localStreamingRestClient.removeStreamingDataListener(this);
     * streamingRestClient = null;
     * }
     * }
     * }
     */

    private void transmitQueue() {
        if (getThing().getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Not transmitting events because bridge is OFFLINE");
            return;
        }

        try {
            while (!LinktapUpdateRequests.isEmpty()) {
                // nestUpdateRequests is a CopyOnWriteArrayList so its iterator does not support remove operations
                LinktapUpdateRequest request = LinktapUpdateRequests.get(0);
                jsonToPutUrl(request);
                LinktapUpdateRequests.remove(request);
            }
        } catch (InvalidConnectionParameters e) {
            logger.debug("Invalid access token", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Token is invalid and could not be refreshed: " + e.getMessage());
        } catch (FailedResolvingLinktapUrlException e) {
            logger.debug("Unable to resolve redirect URL", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduler.schedule(this::restartStreamingUpdates, 5, SECONDS);
        } catch (FailedSendingLinktapDataException e) {
            logger.debug("Error sending data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduler.schedule(this::restartStreamingUpdates, 5, SECONDS);

            LinktapRedirectUrlSupplier localRedirectUrlSupplier = redirectUrlSupplier;
            if (localRedirectUrlSupplier != null) {
                localRedirectUrlSupplier.resetCache();
            }
        }
    }

}