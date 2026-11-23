package com.bonefracture.auth;

import com.bonefracture.persistence.UserEntity;
import com.bonefracture.user.Role;

import java.util.List;

/**
 * SOA: service for creating accounts and logging in.
 */
public interface AuthService {

    /** Creates an account. Throws EmailAlreadyUsedException if the email is taken. */
    UserEntity register(String name, String email, String password, Role role);

    /** Returns a JWT. Throws InvalidCredentialsException if the email or the password is wrong. */
    String login(String email, String password);

    UserEntity getUser(Long id);

    /** Administrator only: marks a doctor account as verified. */
    UserEntity verifyDoctor(String doctorEmail);

    List<UserEntity> unverifiedDoctors();
}
