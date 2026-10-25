package com.bonefracture.appointment;

import com.bonefracture.appointment.state.AppointmentState;
import com.bonefracture.notification.observer.AbstractNotificationSubject;
import com.bonefracture.user.Doctor;
import com.bonefracture.user.Patient;

import java.time.LocalDateTime;

/**
 * STATE pattern: Context.
 * The behaviour of confirm(), cancel() and sendReminder() depends on the current state.
 *
 * OBSERVER pattern: Concrete Subject.
 * Every state change notifies the registered observers (push, e-mail, history).
 */
public class Appointment extends AbstractNotificationSubject {

    private Long id;
    private LocalDateTime dateTime;
    private Doctor doctor;
    private Patient patient;
    private AppointmentState state;

    public Appointment(Doctor doctor, Patient patient, LocalDateTime dateTime) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /** Delegates to the current state. */
    public void confirm() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /** Delegates to the current state. */
    public void cancel() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /** Delegates to the current state. */
    public void sendReminder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /** Called by the concrete states to move the appointment to a new state. */
    public void setState(AppointmentState state) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public AppointmentStatus getStatus() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Long getId() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public LocalDateTime getDateTime() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Doctor getDoctor() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Patient getPatient() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
