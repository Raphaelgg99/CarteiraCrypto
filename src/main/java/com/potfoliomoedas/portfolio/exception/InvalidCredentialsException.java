package com.potfoliomoedas.portfolio.exception;

// Em um novo arquivo: InvalidCredentialsException.java
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
