package com.bonefracture.api;

import com.bonefracture.api.dto.DoctorResponse;
import com.bonefracture.auth.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The verified doctors, so a patient can choose one for an appointment. Needs a JWT.
 */
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final AuthService authService;

    public DoctorController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public List<DoctorResponse> verifiedDoctors() {
        return authService.verifiedDoctors().stream().map(DoctorResponse::from).toList();
    }
}
