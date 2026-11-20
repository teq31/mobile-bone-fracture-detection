package com.bonefracture.persistence;

import com.bonefracture.appointment.AppointmentStatus;
import com.bonefracture.notification.NotificationType;
import com.bonefracture.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that users, appointments and notifications are saved and read back from the database.
 */
@DataJpaTest
class PersistenceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private UserEntity doctor;
    private UserEntity patient;

    @BeforeEach
    void setUp() {
        doctor = userRepository.save(new UserEntity("Dr. Smith", "smith@clinic.com", "hash", Role.DOCTOR));
        patient = userRepository.save(new UserEntity("John Doe", "john@mail.com", "hash", Role.PATIENT));
    }

    @Test
    void userIsFoundByEmail() {
        UserEntity found = userRepository.findByEmail("john@mail.com").orElseThrow();

        assertEquals("John Doe", found.getName());
        assertEquals(Role.PATIENT, found.getRole());
        assertTrue(userRepository.existsByEmail("smith@clinic.com"));
        assertFalse(userRepository.existsByEmail("nobody@mail.com"));
    }

    @Test
    void emailMustBeUnique() {
        assertThrows(DataIntegrityViolationException.class, () ->
                userRepository.saveAndFlush(new UserEntity("Other", "john@mail.com", "hash", Role.PATIENT)));
    }

    @Test
    void newAppointmentIsSavedAsRequested() {
        appointmentRepository.save(new AppointmentEntity(doctor, patient, LocalDateTime.of(2026, 12, 1, 10, 0)));

        List<AppointmentEntity> patientAppointments = appointmentRepository.findByPatientIdOrderByDateTime(patient.getId());
        assertEquals(1, patientAppointments.size());
        assertEquals(AppointmentStatus.REQUESTED, patientAppointments.get(0).getStatus());
        assertEquals(1, appointmentRepository.findByDoctorIdOrderByDateTime(doctor.getId()).size());
    }

    @Test
    void notificationsAreReturnedOnlyForTheirRecipient() {
        notificationRepository.save(new NotificationEntity(patient, NotificationType.APPOINTMENT_CONFIRMED, "confirmed"));
        notificationRepository.save(new NotificationEntity(doctor, NotificationType.APPOINTMENT_REQUESTED, "requested"));

        List<NotificationEntity> patientNotifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(patient.getId());
        assertEquals(1, patientNotifications.size());
        assertEquals(NotificationType.APPOINTMENT_CONFIRMED, patientNotifications.get(0).getType());
        assertFalse(patientNotifications.get(0).isRead());
    }
}
