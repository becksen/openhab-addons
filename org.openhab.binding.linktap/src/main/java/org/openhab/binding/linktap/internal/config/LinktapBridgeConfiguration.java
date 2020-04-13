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
import org.eclipse.jdt.annotation.Nullable;

/**
 * The configuration for the Nest bridge, allowing it to talk to Nest.
 *
 * @author Timmy Becker - Initial contribution
 */
@NonNullByDefault
public class LinktapBridgeConfiguration {
    public static final String GATEWAY_ID = "gatewayId";
    /** Product ID from the Nest product page. */
    public String gatewayId = "";

    public static final String PRODUCT_SECRET = "productSecret";
    /** Product secret from the Nest product page. */
    public String productSecret = "";

    public static final String USERNAME = "usernam";
    /** Product Username from the Nest authorization page. */
    public @Nullable String username;

    public static final String API_KEY = "apiKey";
    /** The access token to use once retrieved from Nest. */
    public @Nullable String apiKey;
}
