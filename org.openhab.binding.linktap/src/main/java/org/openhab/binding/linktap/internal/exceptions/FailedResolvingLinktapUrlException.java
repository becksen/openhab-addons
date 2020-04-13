package org.openhab.binding.linktap.internal.exceptions;

@SuppressWarnings("serial")
public class FailedResolvingLinktapUrlException extends Exception {
    public FailedResolvingLinktapUrlException(String message) {
        super(message);
    }

    public FailedResolvingLinktapUrlException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedResolvingLinktapUrlException(Throwable cause) {
        super(cause);
    }

}
