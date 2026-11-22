package com.bonefracture.api;

import com.bonefracture.api.dto.AdminDtos.VerifyDoctorRequest;
import com.bonefracture.api.dto.AuthDtos.UserResponse;
import com.bonefracture.auth.AuthService;
import com.bonefracture.security.RequiresRole;
import com.bonefracture.user.Role;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Administrator endpoints: doctors waiting for verification and verifying them.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/doctors/unverified")
    @RequiresRole(Role.ADMINISTRATOR)
    public List<UserResponse> unverifiedDoctors() {
        return authService.unverifiedDoctors().stream().map(UserResponse::from).toList();
    }

    @PostMapping("/doctors/verify")
    @RequiresRole(Role.ADMINISTRATOR)
    public UserResponse verifyDoctor(@Valid @RequestBody VerifyDoctorRequest request) {
        return UserResponse.from(authService.verifyDoctor(request.email()));
    }
}
