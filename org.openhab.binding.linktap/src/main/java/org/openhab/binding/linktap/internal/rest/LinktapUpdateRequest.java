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

import java.util.HashMap;
import java.util.Map;

/**
 * Contains the data needed to do an update request back to Nest.
 *
 * @author Timmy Becker - Initial contribution
 */
public class LinktapUpdateRequest {
    private final String updatePath;
    private final Map<String, Object> values;

    private LinktapUpdateRequest(Builder builder) {
        this.updatePath = builder.basePath + builder.identifier;
        this.values = builder.values;
    }

    public String getUpdatePath() {
        return updatePath;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public static class Builder {
        private String basePath;
        private String identifier;
        private Map<String, Object> values = new HashMap<>();

        public Builder withBasePath(String basePath) {
            this.basePath = basePath;
            return this;
        }

        public Builder withIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder withAdditionalValue(String field, Object value) {
            values.put(field, value);
            return this;
        }

        public LinktapUpdateRequest build() {
            return new LinktapUpdateRequest(this);
        }
    }
}
