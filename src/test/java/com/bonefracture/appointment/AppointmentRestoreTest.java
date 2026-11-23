package com.bonefracture.appointment;

import com.bonefracture.user.Doctor;
import com.bonefracture.user.Patient;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * An appointment loaded from the database continues from the state it was saved in.
 */
class AppointmentRestoreTest {

    private final Doctor doctor = new Doctor("Dr. Smith", "smith@clinic.com");
    private final Patient patient = new Patient("John Doe", "john@mail.com");
    private final LocalDateTime dateTime = LocalDateTime.of(2026, 12, 1, 10, 0);

    @Test
    void restoredConfirmedAppointmentFollowsConfirmedRules() {
        Appointment appointment = Appointment.restore(doctor, patient, dateTime, AppointmentStatus.CONFIRMED);

        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        assertThrows(IllegalStateException.class, appointment::confirm);
        appointment.cancel();
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
    }

    @Test
    void restoredCancelledAppointmentAllowsNothing() {
        Appointment appointment = Appointment.restore(doctor, patient, dateTime, AppointmentStatus.CANCELLED);

        assertThrows(IllegalStateException.class, appointment::confirm);
        assertThrows(IllegalStateException.class, appointment::cancel);
        assertThrows(IllegalStateException.class, appointment::sendReminder);
    }
}
