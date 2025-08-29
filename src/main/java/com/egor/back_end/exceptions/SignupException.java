package com.egor.back_end.exceptions;

public class SignupException extends RuntimeException {
    public SignupException(String message) {
        super(message);
    }
}
