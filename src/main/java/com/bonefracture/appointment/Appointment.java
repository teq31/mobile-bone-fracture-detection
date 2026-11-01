package com.bonefracture.appointment;

import com.bonefracture.appointment.state.AppointmentState;
import com.bonefracture.appointment.state.RequestedState;
import com.bonefracture.notification.Notification;
import com.bonefracture.notification.NotificationType;
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
        this.doctor = doctor;
        this.patient = patient;
        this.dateTime = dateTime;
        this.state = RequestedState.INSTANCE;
    }

    /** Delegates to the current state. */
    public void confirm() {
        state.confirm(this);
    }

    /** Delegates to the current state. */
    public void cancel() {
        state.cancel(this);
    }

    /** Delegates to the current state. */
    public void sendReminder() {
        state.sendReminder(this);
    }

    /** Called by the concrete states to move the appointment to a new state. */
    public void setState(AppointmentState state) {
        this.state = state;
    }

    /** Called by the concrete states to tell the observers what changed. */
    public void notifyPatient(NotificationType type, String message) {
        notifyObservers(new Notification(type, patient, message));
    }

    public AppointmentStatus getStatus() {
        return state.getStatus();
    }

    public Long getId() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Patient getPatient() {
        return patient;
    }
}
