package com.bonefracture.appointment.state;

import com.bonefracture.appointment.Appointment;
import com.bonefracture.appointment.AppointmentStatus;

/**
 * STATE pattern: Concrete State.
 * Initial state. Allowed: confirm (-> Confirmed), cancel (-> Cancelled).
 * Not allowed: sendReminder.
 */
public class RequestedState implements AppointmentState {

    @Override
    public void confirm(Appointment appointment) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void cancel(Appointment appointment) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void sendReminder(Appointment appointment) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public AppointmentStatus getStatus() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
