package com.bonefracture.auth;

import com.bonefracture.persistence.UserEntity;
import com.bonefracture.persistence.UserRepository;
import com.bonefracture.security.TokenService;
import com.bonefracture.user.Role;
import com.bonefracture.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SOA: implementation of AuthService. Accounts are created and checked by UserService
 * (Ianis's interface, implemented by DatabaseUserService with his factories); this service
 * adds the rules of the public API and the JWT.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AuthServiceImpl(UserService userService, UserRepository userRepository, TokenService tokenService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    public UserEntity register(String name, String email, String password, Role role) {
        if (role == Role.ADMINISTRATOR) {
            throw new IllegalArgumentException("Administrator accounts cannot be created through registration");
        }
        userService.register(role, name, email, password);
        return userRepository.findByEmail(email).orElseThrow();
    }

    @Override
    public String login(String email, String password) {
        if (!userService.login(email, password)) {
            throw new InvalidCredentialsException();
        }
        return tokenService.createToken(userRepository.findByEmail(email).orElseThrow());
    }

    @Override
    public UserEntity getUser(Long id) {
        return userRepository.findById(id).orElseThrow(InvalidCredentialsException::new);
    }

    @Override
    public UserEntity verifyDoctor(String doctorEmail) {
        return null;
    }

    @Override
    public List<UserEntity> unverifiedDoctors() {
        return List.of();
    }
}
