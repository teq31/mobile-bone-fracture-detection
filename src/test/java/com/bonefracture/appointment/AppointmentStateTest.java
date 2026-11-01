package com.bonefracture.appointment;

import com.bonefracture.user.Doctor;
import com.bonefracture.user.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TDD Iteration 1 - STATE pattern.
 * State transitions: Requested -> Confirmed -> Cancelled.
 * An action that is not allowed in the current state throws IllegalStateException.
 */
class AppointmentStateTest {

    private Appointment appointment;

    @BeforeEach
    void setUp() {
        Doctor doctor = new Doctor("Dr. Smith", "smith@clinic.com");
        Patient patient = new Patient("John Doe", "john@mail.com");
        appointment = new Appointment(doctor, patient, LocalDateTime.of(2026, 11, 10, 10, 0));
    }

    @Test
    void newAppointmentIsRequested() {
        assertEquals(AppointmentStatus.REQUESTED, appointment.getStatus());
    }

    @Test
    void confirmMovesRequestedToConfirmed() {
        appointment.confirm();

        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
    }

    @Test
    void cancelMovesConfirmedToCancelled() {
        appointment.confirm();
        appointment.cancel();

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
    }

    @Test
    void cancelMovesRequestedToCancelled() {
        appointment.cancel();

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
    }

    @Test
    void sendReminderKeepsConfirmedAppointmentConfirmed() {
        appointment.confirm();
        appointment.sendReminder();

        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
    }

    @Test
    void confirmOnConfirmedAppointmentThrows() {
        appointment.confirm();

        assertThrows(IllegalStateException.class, () -> appointment.confirm());
    }

    @Test
    void sendReminderOnRequestedAppointmentThrows() {
        assertThrows(IllegalStateException.class, () -> appointment.sendReminder());
    }

    @Test
    void confirmOnCancelledAppointmentThrows() {
        appointment.cancel();

        assertThrows(IllegalStateException.class, () -> appointment.confirm());
    }

    @Test
    void cancelOnCancelledAppointmentThrows() {
        appointment.cancel();

        assertThrows(IllegalStateException.class, () -> appointment.cancel());
    }

    @Test
    void sendReminderOnCancelledAppointmentThrows() {
        appointment.cancel();

        assertThrows(IllegalStateException.class, () -> appointment.sendReminder());
    }
}
