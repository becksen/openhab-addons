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
package org.openhab.binding.linktap.internal;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link linktapBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Timmy Becker- Initial contribution
 */
@NonNullByDefault
public class linktapBindingConstants {

    private static final String BINDING_ID = "linktap";

    /** REST API URL */
    public static final String REST_URL = "https://www.link-tap.com/api/";

    /** REST API Entity switching watering On and Off */
    public static final String REST_ON_OFF = "activateInstantMode";

    /** REST API Entity to activate interval mode */
    public static final String REST_INTERVAL_MODE = "activateIntervalMode";

    /** REST API Entity to activate odd-even mode */
    public static final String REST_ODD_EVEN_MODE = "activateOddEvenMode";

    /** REST API Entity to activate 7-day mode */
    public static final String REST_SEVEN_DAY_MODE = "activateSevenDayMode";

    /** REST API Entity to activate month mode */
    public static final String REST_MONTH_MODE = "activateMonthMode";

    /** REST API Entity to get all devices */
    public static final String REST_GET_DEVICES = "getAllDevices";

    /** REST API Entity to get watering Status */
    public static final String REST_GET_WATERING_STAT = "getWateringStatus";

    /** REST API call Content-Type in Header */
    public static final String REST_CONTENT_TYPE_PARAM = "application/x-www-form-urlencoded"; // "application/json";

    /** To keep the streaming REST connection alive Nest sends every 30 seconds a message. */
    // might be removed later on
    public static final long KEEP_ALIVE_MILLIS = Duration.ofSeconds(30).toMillis();

    // List of all Thing Type UIDs
    // public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "sample");
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "gateway");
    public static final ThingTypeUID THING_TYPE_STRUCTURE = new ThingTypeUID(BINDING_ID, "structure");

    // supported linktap device Taplinker TP-1B
    public static final ThingTypeUID THING_TYPE_TP1B = new ThingTypeUID(BINDING_ID, "TP1B");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.of(THING_TYPE_BRIDGE, THING_TYPE_TP1B)
            .collect(Collectors.toSet());

    // List of all Channel ids
    // public static final String CHANNEL_1 = "channel1";

    /** Channel for the Bridge/Gateway */
    public static final String CHANNEL_STATUS = "status";

    /** Channels for the Water Timer TP-1B */

    public static final String CHANNEL_WATERING = "watering";

    public static final String CHANNEL_BATTERY_STATUS = "battery-status";

    public static final String CHANNEL_SIGNAL_STRENGTH = "signal";

    public static final String CHANNEL_Location = "location";

    public static final String CHANNEL_POWER = "power";

}
