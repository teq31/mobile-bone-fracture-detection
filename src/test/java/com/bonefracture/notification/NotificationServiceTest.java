package com.bonefracture.notification;

import com.bonefracture.appointment.Appointment;
import com.bonefracture.user.Doctor;
import com.bonefracture.user.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD Iteration 2 - SOA: NotificationService.
 * The service keeps the notification history so a user can see their notifications in the app.
 */
class NotificationServiceTest {

    private NotificationService notificationService;
    private Doctor doctor;
    private LocalDateTime dateTime;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl();
        doctor = new Doctor("Dr. Smith", "smith@clinic.com");
        dateTime = LocalDateTime.of(2026, 11, 10, 10, 0);
    }

    @Test
    void trackedAppointmentNotificationsAreSavedForTheUser() {
        Patient patient = new Patient("John Doe", "john@mail.com");
        Appointment appointment = new Appointment(doctor, patient, dateTime);
        notificationService.track(appointment);

        appointment.confirm();

        List<Notification> notifications = notificationService.getNotificationsFor(patient);
        assertEquals(1, notifications.size());
        assertEquals(NotificationType.APPOINTMENT_CONFIRMED, notifications.get(0).getType());
    }

    @Test
    void getNotificationsForReturnsOnlyThatUsersNotifications() {
        Patient john = new Patient("John Doe", "john@mail.com");
        Patient anna = new Patient("Anna Brown", "anna@mail.com");
        Appointment johnsAppointment = new Appointment(doctor, john, dateTime);
        Appointment annasAppointment = new Appointment(doctor, anna, dateTime);
        notificationService.track(johnsAppointment);
        notificationService.track(annasAppointment);

        johnsAppointment.confirm();
        annasAppointment.cancel();

        List<Notification> annasNotifications = notificationService.getNotificationsFor(anna);
        assertEquals(1, annasNotifications.size());
        assertSame(anna, annasNotifications.get(0).getRecipient());
        assertEquals(NotificationType.APPOINTMENT_CANCELLED, annasNotifications.get(0).getType());
    }

    @Test
    void userWithoutNotificationsGetsEmptyList() {
        Patient patient = new Patient("John Doe", "john@mail.com");

        assertTrue(notificationService.getNotificationsFor(patient).isEmpty());
    }
}
