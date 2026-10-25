package com.bonefracture.user;

public abstract class UserFactory {

    public User register(String name, String email, String password) {
        User user = createUser(name, email);
        user.signUp(password);
        return user;
    }

    protected abstract User createUser(String name, String email);
}
