package com.bonefracture.user;

public class StudentFactory extends UserFactory {

    @Override
    protected User createUser(String name, String email) {
        return new Student(name, email);
    }
}
