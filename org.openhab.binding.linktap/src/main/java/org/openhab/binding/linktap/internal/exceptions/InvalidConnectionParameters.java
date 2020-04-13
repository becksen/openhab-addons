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

package org.openhab.binding.linktap.internal.exceptions;

/**
 * Will be thrown when there is no valid access token and it was not possible to refresh it
 *
 * @author Timmy Becker - Initial contribution
 *
 */
@SuppressWarnings("serial")
public class InvalidConnectionParameters extends Exception {

    public InvalidConnectionParameters(Exception cause) {
        super(cause);
    }

    public InvalidConnectionParameters(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidConnectionParameters(String message) {
        super(message);
    }

}
