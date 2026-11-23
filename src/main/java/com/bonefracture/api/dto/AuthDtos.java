package com.bonefracture.api.dto;

import com.bonefracture.persistence.UserEntity;
import com.bonefracture.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request and response bodies of /api/auth.
 */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, message = "must have at least 8 characters") String password,
            @NotNull Role role) {
    }

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {
    }

    public record UserResponse(Long id, String name, String email, Role role, boolean verified) {

        public static UserResponse from(UserEntity user) {
            return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isVerified());
        }
    }

    public record LoginResponse(String token, String tokenType, long expiresIn) {
    }
}
