package org.openhab.binding.linktap.internal.rest;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class LinktapStreamingRequestFilter implements ClientRequestFilter {
    private final String accessToken;

    public LinktapStreamingRequestFilter(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public void filter(@Nullable ClientRequestContext requestContext) throws IOException {
        if (requestContext != null) {
            MultivaluedMap<String, Object> headers = requestContext.getHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
        }
    }
}
