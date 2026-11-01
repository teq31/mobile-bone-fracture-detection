package com.bonefracture.appointment.state;

import com.bonefracture.appointment.Appointment;
import com.bonefracture.appointment.AppointmentStatus;
import com.bonefracture.notification.Notification;
import com.bonefracture.notification.NotificationType;

/**
 * STATE pattern: Concrete State.
 * Allowed: cancel (-> Cancelled), sendReminder (stays Confirmed).
 * Not allowed: confirm (already confirmed).
 */
public class ConfirmedState implements AppointmentState {

    public static final ConfirmedState INSTANCE = new ConfirmedState();

    @Override
    public void confirm(Appointment appointment) {
        throw new IllegalStateException("Appointment is already confirmed");
    }

    @Override
    public void cancel(Appointment appointment) {
        appointment.setState(CancelledState.INSTANCE);
        appointment.notifyObservers(new Notification(NotificationType.APPOINTMENT_CANCELLED,
                appointment.getPatient(), "Your appointment was cancelled"));
    }

    @Override
    public void sendReminder(Appointment appointment) {
        // stays Confirmed
        appointment.notifyObservers(new Notification(NotificationType.APPOINTMENT_REMINDER,
                appointment.getPatient(), "Reminder: you have an appointment"));
    }

    @Override
    public AppointmentStatus getStatus() {
        return AppointmentStatus.CONFIRMED;
    }
}
