package com.bonefracture.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record VerifyDoctorRequest(@NotBlank @Email String email) {
    }
}
