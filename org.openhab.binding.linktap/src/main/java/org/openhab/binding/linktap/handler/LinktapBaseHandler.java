/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.linktap.handler;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.linktap.internal.config.linktapDeviceConfiguration;
import org.openhab.binding.linktap.internal.data.linktapIdentifiable;
import org.openhab.binding.linktap.internal.listener.linktapThingDataListener;
import org.openhab.binding.linktap.internal.rest.LinktapUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deals with the structures on the Nest API, turning them into a thing in openHAB.
 *
 * @author Timmy Becker - Initial contribution
 *
 * @param <T> the type of update data
 */

@NonNullByDefault
public abstract class LinktapBaseHandler<T> extends BaseThingHandler
        implements linktapThingDataListener<T>, linktapIdentifiable {
    private final Logger logger = LoggerFactory.getLogger(LinktapBaseHandler.class);

    private @Nullable String deviceId;
    private Class<T> dataClass;

    LinktapBaseHandler(Thing thing, Class<T> dataClass) {
        super(thing);
        this.dataClass = dataClass;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for {}", getClass().getName());

        linktapBridgeHandler handler = getLinktapBridgeHandler();
        if (handler != null) {
            boolean success = handler.addThingDataListener(dataClass, getId(), this);
            logger.debug("Adding {} with ID '{}' as device data listener, result: {}", getClass().getSimpleName(),
                    getId(), success);
        } else {
            logger.debug("Unable to add {} with ID '{}' as device data listener because bridge is null",
                    getClass().getSimpleName(), getId());
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Waiting for refresh");

        T lastUpdate = getLastUpdate();
        if (lastUpdate != null) {
            update(null, lastUpdate);
        }
    }

    @Override
    public void dispose() {
        linktapBridgeHandler handler = getLinktapBridgeHandler();
        if (handler != null) {
            handler.removeThingDataListener(dataClass, getId(), this);
        }
    }

    protected @Nullable T getLastUpdate() {
        linktapBridgeHandler handler = getLinktapBridgeHandler();
        if (handler != null) {
            return handler.getLastUpdate(dataClass, getId());
        }
        return null;
    }

    protected void addUpdateRequest(String updatePath, String field, Object value) {
        linktapBridgeHandler handler = getLinktapBridgeHandler();
        if (handler != null) {
            // @formatter:off
            handler.addUpdateRequest(new LinktapUpdateRequest.Builder()
                .withBasePath(updatePath)
                .withIdentifier(getId())
                .withAdditionalValue(field, value)
                .build());
            // @formatter:on
        }
    }

    @Override
    public String getId() {
        return getDeviceId();
    }

    protected String getDeviceId() {
        String localDeviceId = deviceId;
        if (localDeviceId == null) {
            localDeviceId = getConfigAs(linktapDeviceConfiguration.class).deviceId;
            deviceId = localDeviceId;
        }
        return localDeviceId;
    }

    protected @Nullable linktapBridgeHandler getLinktapBridgeHandler() {
        Bridge bridge = getBridge();
        return bridge != null ? (linktapBridgeHandler) bridge.getHandler() : null;
    }

    protected abstract State getChannelState(ChannelUID channelUID, T data);

    protected State getAsDateTimeTypeOrNull(@Nullable Date date) {
        if (date == null) {
            return UnDefType.NULL;
        }

        long offsetMillis = TimeZone.getDefault().getOffset(date.getTime());
        Instant instant = date.toInstant().plusMillis(offsetMillis);
        return new DateTimeType(ZonedDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId()));
    }

    protected State getAsDecimalTypeOrNull(@Nullable Integer value) {
        return value == null ? UnDefType.NULL : new DecimalType(value);
    }

    protected State getAsOnOffTypeOrNull(@Nullable Boolean value) {
        return value == null ? UnDefType.NULL : value ? OnOffType.ON : OnOffType.OFF;
    }

    protected <U extends Quantity<U>> State getAsQuantityTypeOrNull(@Nullable Number value, Unit<U> unit) {
        return value == null ? UnDefType.NULL : new QuantityType<U>(value, unit);
    }

    protected State getAsStringTypeOrNull(@Nullable Object value) {
        return value == null ? UnDefType.NULL : new StringType(value.toString());
    }

    protected State getAsStringTypeListOrNull(@Nullable Collection<?> values) {
        return values == null || values.isEmpty() ? UnDefType.NULL : new StringType(StringUtils.join(values, ","));
    }

    protected boolean isNotHandling(linktapIdentifiable linktapIdentifiable) {
        return !(getId().equals(linktapIdentifiable.getId()));
    }

    protected void updateLinkedChannels(T oldData, T data) {
        getThing().getChannels().stream().map(c -> c.getUID()).filter(this::isLinked).forEach(channelUID -> {
            State newState = getChannelState(channelUID, data);
            if (oldData == null || !getChannelState(channelUID, oldData).equals(newState)) {
                logger.debug("Updating {}", channelUID);
                updateState(channelUID, newState);
            }
        });
    }

    @Override
    public void onNewData(T data) {
        update(null, data);
    }

    @Override
    public void onUpdatedData(T oldData, T data) {
        update(oldData, data);
    }

    @Override
    public void onMissingData(String linktapId) {
        thing.setStatusInfo(
                new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Missing from streaming updates"));
    }

    protected abstract void update(T oldData, T data);

}
