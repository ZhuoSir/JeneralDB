package com.chen.JeneralDB.exception;

/**
 * Created by sunny-chen on 17/1/18.
 */
public class RespositoryException extends Exception {

    private static final long serialVersionUID = 1L;

    public RespositoryException() {
        super("Repository exception");
    }

    public RespositoryException(String message) {
        super(message);
    }

    public RespositoryException(Throwable cause) {
        super(cause);
    }
}
