package com.chwihae.exception;

public class CustomException extends RuntimeException {

    private final CustomExceptionError error;
    private final String message;

    public CustomException(CustomExceptionError error, String message) {
        this.error = error;
        this.message = message;
    }

    public CustomException(CustomExceptionError error) {
        this(error, null);
    }

    @Override
    public String getMessage() {
        String errorMsg = this.error.message();
        if (this.message == null) {
            return errorMsg;
        }
        return String.format("%s (%s)", errorMsg, this.message);
    }

    public String message() {
        return getMessage();
    }

    public CustomExceptionError error() {
        return this.error;
    }
}
