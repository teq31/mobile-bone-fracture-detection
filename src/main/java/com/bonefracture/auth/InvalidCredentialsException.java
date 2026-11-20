package com.bonefracture.auth;

/** Same message for a wrong email and a wrong password, so nobody can find out which emails exist. */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
