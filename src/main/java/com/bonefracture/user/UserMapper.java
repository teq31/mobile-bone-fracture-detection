package com.bonefracture.user;

public final class UserMapper {

    private UserMapper() {
    }

    public static User fromAccount(String name, String email, Role role, boolean verified) {
        return switch (role) {
            case DOCTOR -> {
                Doctor doctor = new Doctor(name, email);
                doctor.setVerified(verified);
                yield doctor;
            }
            case PATIENT -> new Patient(name, email);
            case STUDENT -> new Student(name, email);
            case ADMINISTRATOR -> new Administrator(name, email);
        };
    }
}
