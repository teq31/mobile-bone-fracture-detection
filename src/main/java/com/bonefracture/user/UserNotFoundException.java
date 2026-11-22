package com.bonefracture.user;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String email) {
        super("no user with email " + email);
    }
}
