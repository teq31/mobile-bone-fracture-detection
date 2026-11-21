package com.bonefracture.api;

import com.bonefracture.api.dto.AuthDtos.LoginRequest;
import com.bonefracture.api.dto.AuthDtos.LoginResponse;
import com.bonefracture.api.dto.AuthDtos.RegisterRequest;
import com.bonefracture.api.dto.AuthDtos.UserResponse;
import com.bonefracture.auth.AuthService;
import com.bonefracture.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/auth/register, POST /api/auth/login (returns a JWT), GET /api/auth/me (needs the JWT).
 * Errors are turned into HTTP responses by ApiExceptionHandler.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return UserResponse.from(authService.register(request.name(), request.email(), request.password(), request.role()));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return new LoginResponse(token, "Bearer", tokenService.getValiditySeconds());
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return UserResponse.from(authService.getUser(Long.valueOf(jwt.getSubject())));
    }
}
