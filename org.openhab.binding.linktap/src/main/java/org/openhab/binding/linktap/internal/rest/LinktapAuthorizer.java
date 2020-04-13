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
package org.openhab.binding.linktap.internal.rest;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.jetbrains.annotations.Nullable;
import org.openhab.binding.linktap.internal.LinktapUtils;
import org.openhab.binding.linktap.internal.linktapBindingConstants;
import org.openhab.binding.linktap.internal.config.LinktapBridgeConfiguration;
import org.openhab.binding.linktap.internal.data.AccessTokenData;
import org.openhab.binding.linktap.internal.exceptions.InvalidAccessTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves the Nest access token using the OAuth 2.0 protocol using pin-based authorization.
 *
 * @author Timmy Becker - Initial contribution
 */
@NonNullByDefault
public class LinktapAuthorizer {

    private final Logger logger = LoggerFactory.getLogger(LinktapAuthorizer.class);

    private final LinktapBridgeConfiguration config;

    /**
     * Create the helper class for the Nest access token. Also creates the folder
     * to put the access token data in if it does not already exist.
     *
     * @param config The configuration to use for the token
     */
    public LinktapAuthorizer(LinktapBridgeConfiguration config) {
        this.config = config;
    }

    @NonNullByDefault
    public @Nullable String getApiKey() throws InvalidAccessTokenException {
        String key = "12234455";
        key = config.apiKey;

        if (key == null) {
            return "123456";
        }
        return key;
    }

    /**
     * Get the current access token, refreshing if needed.
     *
     * @throws InvalidAccessTokenException thrown when the access token is invalid and could not be refreshed
     */
    public String getNewAccessToken() throws InvalidAccessTokenException {
        try {
            if (StringUtils.isEmpty(config.username)) {
                throw new InvalidAccessTokenException("Accesstoken is empty");
            }

            // @formatter:off
            StringBuilder urlBuilder = new StringBuilder(linktapBindingConstants.REST_URL)
                    .append("?client_id=")
                    .append(config.gatewayId)
                    .append("&client_secret=")
                    .append(config.productSecret)
                    .append("&code=")
                    .append(config.username)
                    .append("&grant_type=authorization_code");
            // @formatter:on

            logger.debug("Requesting access token from URL: {}", urlBuilder);

            String responseContentAsString = HttpUtil.executeUrl("POST", urlBuilder.toString(), null, null,
                    "application/x-www-form-urlencoded", 10_000);

            AccessTokenData data = LinktapUtils.fromJson(responseContentAsString, AccessTokenData.class);
            logger.debug("Received: {}", data);

            if (StringUtils.isEmpty(data.getAccessToken())) {
                throw new InvalidAccessTokenException("Pincode to obtain access token is already used or invalid)");
            }

            return data.getAccessToken();
        } catch (IOException e) {
            throw new InvalidAccessTokenException("Access token request failed", e);
        }
    }
}
