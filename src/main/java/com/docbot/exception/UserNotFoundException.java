package com.docbot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested User resource cannot be found.
 * Annotated with @ResponseStatus so Spring returns HTTP 404 when this exception is thrown
 * from a controller/service call handled by the web layer.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() { super(); }

    public UserNotFoundException(String message) { super(message); }

    public UserNotFoundException(String message, Throwable cause) { super(message, cause); }
}
