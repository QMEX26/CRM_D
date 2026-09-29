package com.crm.exception;

public class GoogleSheetsSyncException extends RuntimeException {
    public GoogleSheetsSyncException(String message) {
        super(message);
    }

    public GoogleSheetsSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
