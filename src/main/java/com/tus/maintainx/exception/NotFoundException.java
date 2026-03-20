/**
 * Exception class for not found.
 * Represents specific error cases in this module.
 */

package com.tus.maintainx.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String msg) {
        super(msg);
    }
}