package com.bonefracture.api.dto;

import com.bonefracture.persistence.UserEntity;

/**
 * A doctor in the list a patient chooses from (GET /api/doctors).
 */
public record DoctorResponse(Long id, String name, String email) {

    public static DoctorResponse from(UserEntity doctor) {
        return new DoctorResponse(doctor.getId(), doctor.getName(), doctor.getEmail());
    }
}
