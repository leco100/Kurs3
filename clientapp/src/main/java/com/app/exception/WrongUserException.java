package com.app.exception;

public class WrongUserException extends Exception {
    public WrongUserException() {
    }

    public WrongUserException(String message) {
        super(message);
    }
}
