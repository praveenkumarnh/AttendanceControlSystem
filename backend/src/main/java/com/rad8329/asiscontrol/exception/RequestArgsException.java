package com.rad8329.asiscontrol.exception;

@SuppressWarnings("unused")
public class RequestArgsException extends Exception {

    private final int code;

    public RequestArgsException() {
        super();
        code = 500;
    }

    public RequestArgsException(int code) {
        super();
        this.code = code;
    }

    public RequestArgsException(int code, String message) {
        super(message);
        this.code = code;
    }

    public RequestArgsException(int code, String message, Throwable cause) {

        super(message, cause);
        this.code = code;
    }

    public RequestArgsException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public RequestArgsException(int code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
