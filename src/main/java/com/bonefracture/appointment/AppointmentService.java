package com.bonefracture.appointment;

import com.bonefracture.user.Doctor;
import com.bonefracture.user.Patient;

import java.time.LocalDateTime;

/**
 * SOA: service for booking and managing appointments between a patient and a doctor.
 */
public interface AppointmentService {

    Appointment requestAppointment(Doctor doctor, Patient patient, LocalDateTime dateTime);

    void confirm(Appointment appointment);

    void cancel(Appointment appointment);

    void sendReminder(Appointment appointment);
}
