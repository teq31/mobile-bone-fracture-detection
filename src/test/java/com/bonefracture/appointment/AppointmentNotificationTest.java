package com.bonefracture.appointment;

import com.bonefracture.notification.Notification;
import com.bonefracture.notification.NotificationType;
import com.bonefracture.notification.observer.NotificationObserver;
import com.bonefracture.user.Doctor;
import com.bonefracture.user.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD Iteration 2 - OBSERVER pattern.
 * Observers receive a notification with the correct type on every change; attach() and detach().
 */
class AppointmentNotificationTest {

    private Patient patient;
    private Appointment appointment;
    private List<Notification> received;
    private NotificationObserver observer;

    @BeforeEach
    void setUp() {
        Doctor doctor = new Doctor("Dr. Smith", "smith@clinic.com");
        patient = new Patient("John Doe", "john@mail.com");
        appointment = new Appointment(doctor, patient, LocalDateTime.of(2026, 11, 10, 10, 0));
        received = new ArrayList<>();
        observer = received::add;
        appointment.attach(observer);
    }

    @Test
    void confirmNotifiesObserverWithAppointmentConfirmed() {
        appointment.confirm();

        assertEquals(1, received.size());
        assertEquals(NotificationType.APPOINTMENT_CONFIRMED, received.get(0).getType());
        assertSame(patient, received.get(0).getRecipient());
    }

    @Test
    void cancelNotifiesObserverWithAppointmentCancelled() {
        appointment.cancel();

        assertEquals(1, received.size());
        assertEquals(NotificationType.APPOINTMENT_CANCELLED, received.get(0).getType());
    }

    @Test
    void sendReminderNotifiesObserverWithAppointmentReminder() {
        appointment.confirm();
        appointment.sendReminder();

        assertEquals(2, received.size());
        assertEquals(NotificationType.APPOINTMENT_REMINDER, received.get(1).getType());
    }

    @Test
    void allAttachedObserversAreNotified() {
        List<Notification> receivedBySecond = new ArrayList<>();
        appointment.attach(receivedBySecond::add);

        appointment.confirm();

        assertEquals(1, received.size());
        assertEquals(1, receivedBySecond.size());
    }

    @Test
    void detachedObserverIsNotNotified() {
        appointment.detach(observer);

        appointment.confirm();

        assertTrue(received.isEmpty());
    }

    @Test
    void actionNotAllowedSendsNoNotification() {
        assertThrows(IllegalStateException.class, () -> appointment.sendReminder());

        assertTrue(received.isEmpty());
    }
}
