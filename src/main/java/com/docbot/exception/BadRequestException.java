package com.docbot.exception;

/**
 * Exception representing a client-side bad request (HTTP 400).
 * Thrown when request data is invalid (for example, malformed UUIDs).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException() { super(); }

    public BadRequestException(String message) { super(message); }

    public BadRequestException(String message, Throwable cause) { super(message, cause); }
}
