package org.openhab.binding.linktap.internal.exceptions;

@SuppressWarnings("serial")
public class FailedSendingLinktapDataException extends Exception {
    public FailedSendingLinktapDataException(String message) {
        super(message);
    }

    public FailedSendingLinktapDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedSendingLinktapDataException(Throwable cause) {
        super(cause);
    }
}
