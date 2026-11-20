package com.bonefracture.booking;

import com.bonefracture.persistence.AppointmentEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SOA: appointments saved in the database, used by the REST API.
 * The rules for each action come from the STATE classes, through AppointmentService.
 *
 * Errors: AppointmentNotFoundException, NotAllowedException (wrong user or role),
 * IllegalStateException (action not allowed in the current state),
 * IllegalArgumentException (invalid data, e.g. the chosen user is not a doctor).
 */
public interface AppointmentBookingService {

    /** A patient asks for an appointment with a doctor. */
    AppointmentEntity request(Long patientId, Long doctorId, LocalDateTime dateTime);

    /** Only the doctor of the appointment can confirm it. */
    AppointmentEntity confirm(Long appointmentId, Long userId);

    /** The patient or the doctor of the appointment can cancel it. */
    AppointmentEntity cancel(Long appointmentId, Long userId);

    /** Only the doctor of the appointment can send a reminder. */
    AppointmentEntity sendReminder(Long appointmentId, Long userId);

    AppointmentEntity get(Long appointmentId, Long userId);

    /** The appointments of a patient, or of a doctor, ordered by date. */
    List<AppointmentEntity> findForUser(Long userId);
}
