package org.openhab.binding.linktap.internal.rest;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.linktap.internal.LinktapUtils;
import org.openhab.binding.linktap.internal.linktapBindingConstants;
import org.openhab.binding.linktap.internal.config.LinktapBridgeConfiguration;
import org.openhab.binding.linktap.internal.data.AccessTokenData;
import org.openhab.binding.linktap.internal.exceptions.InvalidConnectionParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves the Nest access token using the OAuth 2.0 protocol using pin-based authorization.
 *
 * @author Timmy Becker - Initial contribution
 */
@NonNullByDefault
public class LinktapConnectionTest {

    private final Logger logger = LoggerFactory.getLogger(LinktapConnectionTest.class);

    private final LinktapBridgeConfiguration config;

    public LinktapConnectionTest(LinktapBridgeConfiguration config) {
        this.config = config;
    }

    public String getApiKey() throws InvalidConnectionParameters {
        String key = "12234455";
        key = config.apiKey;

        if (key == null) {
            return "123456";
        }
        return key;
    }

    /**
     * Get connection parameters from Gateway and perform connection test.
     *
     * @throws InvalidConnectionParameters thrown when username or apikey are not valid
     */
    // public String getNewAccessToken() throws InvalidAccessTokenException {
    public String getUsername() throws InvalidConnectionParameters {
        try {
            if (StringUtils.isEmpty(config.username)) {
                throw new InvalidConnectionParameters("Username is empty");
            }

            // @formatter:off


            StringBuilder urlBuilder = new StringBuilder(linktapBindingConstants.REST_URL)
                    .append(linktapBindingConstants.REST_GET_DEVICES)
                    .append("?username=")
                    .append(config.username)
                    .append("&capiKey=")
                    .append(config.apiKey);

            /*StringBuilder payload = new StringBuilder("\"username=")
                    .append(config.username)
                    .append("&apkey=")
                    .append(config.apiKey)
                    .append("\"")
                    .append(config.productSecret)
                    .append("&code=")
                    .append(config.username)
                    .append("&grant_type=authorization_code");*/
            // @formatter:on

            // @formatter:on

            logger.debug("Requesting access token from URL: {}", urlBuilder);

            String responseContentAsString = HttpUtil.executeUrl("POST", urlBuilder.toString(), null, null,
                    linktapBindingConstants.REST_CONTENT_TYPE_PARAM, 10_000);
            // TODO replace AccessTokenData Class
            AccessTokenData data = LinktapUtils.fromJson(responseContentAsString, AccessTokenData.class);
            logger.debug("Received: {}", data);

            if (StringUtils.isEmpty(config.apiKey)) {
                throw new InvalidConnectionParameters("ApiKey is empty)");
            }

            return data.getAccessToken();
        } catch (IOException e) {
            throw new InvalidConnectionParameters("Access token request failed", e);
        }

    }
}
