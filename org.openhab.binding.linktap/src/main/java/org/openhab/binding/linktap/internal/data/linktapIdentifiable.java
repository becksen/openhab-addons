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
package org.openhab.binding.linktap.internal.data;

/**
 * Interface for uniquely identifiable Nest objects (device or a structure).
 *
 * @author Timmy Becker- Initial contribution
 */
public interface linktapIdentifiable {

    /**
     * Returns the identifier that uniquely identifies the linktap object (deviceId or structureId).
     */
    String getId();
}
