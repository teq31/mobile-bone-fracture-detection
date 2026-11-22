package com.bonefracture.api.dto;

import com.bonefracture.audit.AuditEntry;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record VerifyDoctorRequest(@NotBlank @Email String email) {
    }

    public record AuditEntryResponse(Instant time, String actor, String action, String target, boolean success) {

        public static AuditEntryResponse from(AuditEntry entry) {
            return new AuditEntryResponse(entry.time(), entry.actor(), entry.action(), entry.target(), entry.success());
        }
    }
}
