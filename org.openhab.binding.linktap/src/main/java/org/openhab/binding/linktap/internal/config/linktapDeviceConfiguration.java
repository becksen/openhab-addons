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

package org.openhab.binding.linktap.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The configuration for Nest devices.
 *
 * @author Timmy Becker - Initial contribution
 */

@NonNullByDefault
public class linktapDeviceConfiguration {
    public static final String DEVICE_ID = "deviceId";
    /** Device ID which can be retrieved with the Nest API. */
    public String deviceId = "";
}
