package com.bonefracture.api.dto;

import com.bonefracture.appointment.AppointmentStatus;
import com.bonefracture.persistence.AppointmentEntity;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Request and response bodies of /api/appointments.
 */
public final class AppointmentDtos {

    private AppointmentDtos() {
    }

    public record CreateAppointmentRequest(@NotNull Long doctorId, @NotNull @Future LocalDateTime dateTime) {
    }

    public record AppointmentResponse(Long id, Long doctorId, String doctorName, Long patientId, String patientName,
                                      LocalDateTime dateTime, AppointmentStatus status) {

        public static AppointmentResponse from(AppointmentEntity appointment) {
            return new AppointmentResponse(appointment.getId(),
                    appointment.getDoctor().getId(), appointment.getDoctor().getName(),
                    appointment.getPatient().getId(), appointment.getPatient().getName(),
                    appointment.getDateTime(), appointment.getStatus());
        }
    }
}
