package com.bonefracture.booking;

/** The user is logged in but may not do this action (wrong role, or not their appointment). */
public class NotAllowedException extends RuntimeException {

    public NotAllowedException(String message) {
        super(message);
    }
}
