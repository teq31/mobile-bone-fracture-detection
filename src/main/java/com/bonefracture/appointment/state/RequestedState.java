package com.bonefracture.appointment.state;

import com.bonefracture.appointment.Appointment;
import com.bonefracture.appointment.AppointmentStatus;
import com.bonefracture.notification.Notification;
import com.bonefracture.notification.NotificationType;

/**
 * STATE pattern: Concrete State.
 * Initial state. Allowed: confirm (-> Confirmed), cancel (-> Cancelled).
 * Not allowed: sendReminder.
 */
public class RequestedState implements AppointmentState {

    /** States have no fields, so one shared instance is enough (GoF State: sharing state objects). */
    public static final RequestedState INSTANCE = new RequestedState();

    @Override
    public void confirm(Appointment appointment) {
        appointment.setState(ConfirmedState.INSTANCE);
        appointment.notifyObservers(new Notification(NotificationType.APPOINTMENT_CONFIRMED,
                appointment.getPatient(), "Your appointment was confirmed"));
    }

    @Override
    public void cancel(Appointment appointment) {
        appointment.setState(CancelledState.INSTANCE);
        appointment.notifyObservers(new Notification(NotificationType.APPOINTMENT_CANCELLED,
                appointment.getPatient(), "Your appointment was cancelled"));
    }

    @Override
    public void sendReminder(Appointment appointment) {
        throw new IllegalStateException("Cannot send a reminder for an appointment that is not confirmed");
    }

    @Override
    public AppointmentStatus getStatus() {
        return AppointmentStatus.REQUESTED;
    }
}
