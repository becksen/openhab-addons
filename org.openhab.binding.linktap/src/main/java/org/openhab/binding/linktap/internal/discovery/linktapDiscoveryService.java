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
package org.openhab.binding.linktap.internal.discovery;

import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;
import static org.openhab.binding.linktap.internal.linktapBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.linktap.handler.linktapBridgeHandler;
import org.openhab.binding.linktap.internal.config.LinktapStructureConfiguration;
import org.openhab.binding.linktap.internal.config.linktapDeviceConfiguration;
import org.openhab.binding.linktap.internal.data.BaseLinktapDevice;
import org.openhab.binding.linktap.internal.data.Structure;
import org.openhab.binding.linktap.internal.data.TP1B;
import org.openhab.binding.linktap.internal.listener.linktapThingDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;

/**
 * This service connects to the Linktap Gateway and creates the correct discovery results for Linktap devices
 * as they are found through the API.
 *
 * @author Timmy Becker - Initial contribution
 *
 */

@NonNullByDefault
public class linktapDiscoveryService extends AbstractDiscoveryService {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream.of(THING_TYPE_TP1B, THING_TYPE_BRIDGE)
            .collect(Collectors.toSet());

    private final Logger logger = LoggerFactory.getLogger(linktapDiscoveryService.class);

    private final DiscoveryDataListener<TP1B> watertimerDiscoveryDataListener = new DiscoveryDataListener<>(TP1B.class,
            THING_TYPE_TP1B, this::addDeviceDiscoveryResult);

    @SuppressWarnings("rawtypes")
    private final List<DiscoveryDataListener> discoveryDataListeners = Stream.of(watertimerDiscoveryDataListener)
            .collect(Collectors.toList());

    private final linktapBridgeHandler bridge;

    private static class DiscoveryDataListener<T> implements linktapThingDataListener<T> {
        private Class<T> dataClass;
        private ThingTypeUID thingTypeUID;
        private BiConsumer<T, ThingTypeUID> onDiscovered;

        private DiscoveryDataListener(Class<T> dataClass, ThingTypeUID thingTypeUID,
                BiConsumer<T, ThingTypeUID> onDiscovered) {
            this.dataClass = dataClass;
            this.thingTypeUID = thingTypeUID;
            this.onDiscovered = onDiscovered;
        }

        @Override
        public void onNewData(T data) {
            onDiscovered.accept(data, thingTypeUID);
        }

        @Override
        public void onUpdatedData(T oldData, T data) {
        }

        @Override
        public void onMissingData(String linktapId) {
        }
    }

    public linktapDiscoveryService(linktapBridgeHandler bridge) {
        super(SUPPORTED_THING_TYPES, 60, true);
        this.bridge = bridge;
    }

    @SuppressWarnings("unchecked")
    public void activate() {
        discoveryDataListeners.forEach(l -> bridge.addThingDataListener(l.dataClass, l));
        addDiscoveryResultsFromLastUpdates();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void deactivate() {
        discoveryDataListeners.forEach(l -> bridge.removeThingDataListener(l.dataClass, l));
    }

    @Override
    protected void startScan() {
        addDiscoveryResultsFromLastUpdates();
    }

    @SuppressWarnings("unchecked")
    private void addDiscoveryResultsFromLastUpdates() {
        discoveryDataListeners
                .forEach(l -> addDiscoveryResultsFromLastUpdates(l.dataClass, l.thingTypeUID, l.onDiscovered));
    }

    private <T> void addDiscoveryResultsFromLastUpdates(Class<T> dataClass, ThingTypeUID thingTypeUID,
            BiConsumer<T, ThingTypeUID> onDiscovered) {
        List<T> lastUpdates = bridge.getLastUpdates(dataClass);
        lastUpdates.forEach(lastUpdate -> onDiscovered.accept(lastUpdate, thingTypeUID));
    }

    private void addDeviceDiscoveryResult(BaseLinktapDevice device, ThingTypeUID typeUID) {
        ThingUID bridgeUID = bridge.getThing().getUID();
        ThingUID thingUID = new ThingUID(typeUID, bridgeUID, device.getDeviceId());
        logger.debug("Discovered {}", thingUID);
        Map<String, Object> properties = new HashMap<>();
        properties.put(linktapDeviceConfiguration.DEVICE_ID, device.getDeviceId());
        properties.put(PROPERTY_FIRMWARE_VERSION, device.getSoftwareVersion());
        // @formatter:off
        thingDiscovered(DiscoveryResultBuilder.create(thingUID)
                .withThingType(typeUID)
                .withLabel(device.getNameLong())
                .withBridge(bridgeUID)
                .withProperties(properties)
                .withRepresentationProperty(linktapDeviceConfiguration.DEVICE_ID)
                .build()
        );
        // @formatter:on
    }

    public void addStructureDiscoveryResult(Structure structure, ThingTypeUID typeUID) {
        ThingUID bridgeUID = bridge.getThing().getUID();
        ThingUID thingUID = new ThingUID(typeUID, bridgeUID, structure.getStructureId());
        logger.debug("Discovered {}", thingUID);
        Map<String, Object> properties = new HashMap<>();
        properties.put(LinktapStructureConfiguration.STRUCTURE_ID, structure.getStructureId());
        // @formatter:off
        thingDiscovered(DiscoveryResultBuilder.create(thingUID)
                .withThingType(THING_TYPE_STRUCTURE)
                .withLabel(structure.getName())
                .withBridge(bridgeUID)
                .withProperties(properties)
                .withRepresentationProperty(LinktapStructureConfiguration.STRUCTURE_ID)
                .build()
        );
        // @formatter:on
    }
}
