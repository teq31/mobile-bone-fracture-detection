package com.bonefracture.user;

public class DoctorFactory extends UserFactory {
    @Override
    protected User createUser(String name, String email) {
        return new Doctor(name, email); // not verified yet
    }
}
