package com.bonefracture.appointment;

import com.bonefracture.user.Doctor;
import com.bonefracture.user.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TDD Iteration 1, cycle 2 - SOA: AppointmentService.
 * The service is the entry point for working with appointments.
 */
class AppointmentServiceTest {

    private AppointmentService appointmentService;
    private Doctor doctor;
    private Patient patient;
    private LocalDateTime dateTime;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentServiceImpl();
        doctor = new Doctor("Dr. Smith", "smith@clinic.com");
        patient = new Patient("John Doe", "john@mail.com");
        dateTime = LocalDateTime.of(2026, 11, 10, 10, 0);
        appointment = appointmentService.requestAppointment(doctor, patient, dateTime);
    }

    @Test
    void requestAppointmentCreatesRequestedAppointment() {
        assertEquals(AppointmentStatus.REQUESTED, appointment.getStatus());
        assertSame(doctor, appointment.getDoctor());
        assertSame(patient, appointment.getPatient());
        assertEquals(dateTime, appointment.getDateTime());
    }

    @Test
    void confirmMovesAppointmentToConfirmed() {
        appointmentService.confirm(appointment);

        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
    }

    @Test
    void cancelMovesAppointmentToCancelled() {
        appointmentService.cancel(appointment);

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
    }

    @Test
    void sendReminderKeepsConfirmedAppointmentConfirmed() {
        appointmentService.confirm(appointment);

        appointmentService.sendReminder(appointment);

        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
    }

    @Test
    void actionNotAllowedThroughServiceThrows() {
        assertThrows(IllegalStateException.class, () -> appointmentService.sendReminder(appointment));
    }
}
