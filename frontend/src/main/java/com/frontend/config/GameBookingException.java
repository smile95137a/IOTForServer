package com.frontend.config;

public class GameBookingException extends RuntimeException {
    
    public GameBookingException() {
        super();
    }

    public GameBookingException(String message) {
        super(message);
    }

    public GameBookingException(String message, Throwable cause) {
        super(message, cause);
    }
}
