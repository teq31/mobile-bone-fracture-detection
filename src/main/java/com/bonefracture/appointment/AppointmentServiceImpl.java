package com.bonefracture.appointment;

import com.bonefracture.user.Doctor;
import com.bonefracture.user.Patient;

import java.time.LocalDateTime;

/**
 * SOA: implementation of AppointmentService.
 * The rules for each action are decided by the appointment's current state (STATE pattern).
 */
public class AppointmentServiceImpl implements AppointmentService {

    @Override
    public Appointment requestAppointment(Doctor doctor, Patient patient, LocalDateTime dateTime) {
        return new Appointment(doctor, patient, dateTime);
    }

    @Override
    public void confirm(Appointment appointment) {
        appointment.confirm();
    }

    @Override
    public void cancel(Appointment appointment) {
        appointment.cancel();
    }

    @Override
    public void sendReminder(Appointment appointment) {
        appointment.sendReminder();
    }
}
