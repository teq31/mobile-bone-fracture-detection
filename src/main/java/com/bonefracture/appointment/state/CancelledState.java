package com.bonefracture.appointment.state;

import com.bonefracture.appointment.Appointment;
import com.bonefracture.appointment.AppointmentStatus;

/**
 * STATE pattern: Concrete State.
 * Final state. No action is allowed anymore.
 */
public class CancelledState implements AppointmentState {

    @Override
    public void confirm(Appointment appointment) {
        throw new IllegalStateException("Cannot confirm a cancelled appointment");
    }

    @Override
    public void cancel(Appointment appointment) {
        throw new IllegalStateException("Appointment is already cancelled");
    }

    @Override
    public void sendReminder(Appointment appointment) {
        throw new IllegalStateException("Cannot send a reminder for a cancelled appointment");
    }

    @Override
    public AppointmentStatus getStatus() {
        return AppointmentStatus.CANCELLED;
    }
}
