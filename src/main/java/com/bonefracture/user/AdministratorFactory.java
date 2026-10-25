package com.bonefracture.user;

public class AdministratorFactory extends UserFactory {

    @Override
    protected User createUser(String name, String email) {
        return new Administrator(name, email);
    }
}
