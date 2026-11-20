package com.bonefracture.auth;

import com.bonefracture.persistence.UserEntity;
import com.bonefracture.persistence.UserRepository;
import com.bonefracture.security.TokenService;
import com.bonefracture.user.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SOA: implementation of AuthService. Passwords are stored as BCrypt hashes, never in plain text.
 * A new doctor is not verified; an administrator verifies doctors.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    public UserEntity register(String name, String email, String password, Role role) {
        if (role == Role.ADMINISTRATOR) {
            throw new IllegalArgumentException("Administrator accounts cannot be created through registration");
        }
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        return userRepository.save(new UserEntity(name, email, passwordEncoder.encode(password), role));
    }

    @Override
    public String login(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .filter(found -> passwordEncoder.matches(password, found.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        return tokenService.createToken(user);
    }

    @Override
    public UserEntity getUser(Long id) {
        return userRepository.findById(id).orElseThrow(InvalidCredentialsException::new);
    }
}
