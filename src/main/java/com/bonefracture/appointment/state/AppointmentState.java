package com.bonefracture.appointment.state;

import com.bonefracture.appointment.Appointment;
import com.bonefracture.appointment.AppointmentStatus;

/**
 * STATE pattern: State interface.
 * Each concrete state decides which actions are allowed and what the next state is.
 */
public interface AppointmentState {

    void confirm(Appointment appointment);

    void cancel(Appointment appointment);

    void sendReminder(Appointment appointment);

    AppointmentStatus getStatus();

    /** The state object for a status saved in the database. */
    static AppointmentState forStatus(AppointmentStatus status) {
        return switch (status) {
            case REQUESTED -> RequestedState.INSTANCE;
            case CONFIRMED -> ConfirmedState.INSTANCE;
            case CANCELLED -> CancelledState.INSTANCE;
        };
    }
}
