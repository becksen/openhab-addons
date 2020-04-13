package org.openhab.binding.linktap.handler;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.linktap.internal.linktapBindingConstants;
import org.openhab.binding.linktap.internal.exceptions.FailedResolvingLinktapUrlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class LinktapRedirectUrlSupplier {
    private final Logger logger = LoggerFactory.getLogger(LinktapRedirectUrlSupplier.class);

    protected String cachedUrl = "";

    protected Properties httpHeaders;

    public LinktapRedirectUrlSupplier(Properties httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public String getRedirectUrl() throws FailedResolvingLinktapUrlException {
        if (cachedUrl.isEmpty()) {
            cachedUrl = resolveRedirectUrl();
        }
        return cachedUrl;
    }

    public void resetCache() {
        cachedUrl = "";
    }

    /**
     * Resolves the redirect URL for calls using the {@link NestBindingConstants#REST_URL}.
     *
     * The Jetty client used by {@link HttpUtil} will not pass the Authorization header after a redirect resulting in
     * "401 Unauthorized error" issues.
     *
     * Note that this workaround currently does not use any configured proxy like {@link HttpUtil} does.
     *
     * @see https://developers.nest.com/documentation/cloud/how-to-handle-redirects
     */
    private String resolveRedirectUrl() throws FailedResolvingLinktapUrlException {
        HttpClient httpClient = new HttpClient(new SslContextFactory());
        httpClient.setFollowRedirects(false);

        Request request = httpClient.newRequest(linktapBindingConstants.REST_URL).method(HttpMethod.GET).timeout(30,
                TimeUnit.SECONDS);
        for (String httpHeaderKey : httpHeaders.stringPropertyNames()) {
            request.header(httpHeaderKey, httpHeaders.getProperty(httpHeaderKey));
        }

        ContentResponse response;
        try {
            httpClient.start();
            response = request.send();
            httpClient.stop();
        } catch (Exception e) {
            throw new FailedResolvingLinktapUrlException("Failed to resolve redirect URL: " + e.getMessage(), e);
        }

        int status = response.getStatus();
        String redirectUrl = response.getHeaders().get(HttpHeader.LOCATION);

        if (status != HttpStatus.TEMPORARY_REDIRECT_307) {
            logger.debug("Redirect status: {}", status);
            logger.debug("Redirect response: {}", response.getContentAsString());
            throw new FailedResolvingLinktapUrlException("Failed to get redirect URL, expected status "
                    + HttpStatus.TEMPORARY_REDIRECT_307 + " but was " + status);
        } else if (StringUtils.isEmpty(redirectUrl)) {
            throw new FailedResolvingLinktapUrlException("Redirect URL is empty");
        }

        redirectUrl = redirectUrl.endsWith("/") ? redirectUrl.substring(0, redirectUrl.length() - 1) : redirectUrl;
        logger.debug("Redirect URL: {}", redirectUrl);
        return redirectUrl;
    }
}
