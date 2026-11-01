package com.bonefracture.appointment.state;

import com.bonefracture.appointment.Appointment;
import com.bonefracture.appointment.AppointmentStatus;

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
    }

    @Override
    public void sendReminder(Appointment appointment) {
        // stays Confirmed
    }

    @Override
    public AppointmentStatus getStatus() {
        return AppointmentStatus.CONFIRMED;
    }
}
