package com.zephyr.migration.exception;

/**
 * @author Vivek Srivastava.
 */
public class NDataException extends RuntimeException {

    private static final long serialVersionUID = 930756950715810429L;

    public NDataException() {
    }

    public NDataException(String message) {
        super(message);
    }

    public NDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public NDataException(Throwable cause) {
        super(cause);
    }

    public NDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
