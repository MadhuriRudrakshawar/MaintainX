/**
 * Exception class for bad request.
 * Represents specific error cases in this module.
 */

package com.tus.maintainx.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String msg) {
        super(msg);
    }
}
