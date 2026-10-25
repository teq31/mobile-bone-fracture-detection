package com.bonefracture.user;

public class PatientFactory extends UserFactory {

    @Override
    protected User createUser(String name, String email) {
        return new Patient(name, email);
    }
}
