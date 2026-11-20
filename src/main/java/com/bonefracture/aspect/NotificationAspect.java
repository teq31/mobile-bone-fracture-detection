package com.bonefracture.aspect;

import com.bonefracture.appointment.Appointment;
import com.bonefracture.notification.NotificationType;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * AOP: sends the notification after an appointment changes, so the STATE classes and the
 * services contain no notification code.
 *
 * - @AfterReturning: only when the action succeeded; if the state throws IllegalStateException,
 *   nothing is sent.
 * - The notification goes to the appointment's observers (OBSERVER pattern), which deliver it
 *   (e.g. DatabaseNotificationObserver saves it for the app).
 *
 * Woven at compile time by the AspectJ compiler (aspectj-maven-plugin).
 */
@Aspect
public class NotificationAspect {

    @Pointcut("execution(void com.bonefracture.appointment.Appointment.confirm()) && this(appointment)")
    void appointmentConfirmed(Appointment appointment) {
    }

    @Pointcut("execution(void com.bonefracture.appointment.Appointment.cancel()) && this(appointment)")
    void appointmentCancelled(Appointment appointment) {
    }

    @Pointcut("execution(void com.bonefracture.appointment.Appointment.sendReminder()) && this(appointment)")
    void reminderSent(Appointment appointment) {
    }

    @AfterReturning(pointcut = "appointmentConfirmed(appointment)", argNames = "appointment")
    public void notifyConfirmed(Appointment appointment) {
        appointment.notifyPatient(NotificationType.APPOINTMENT_CONFIRMED,
                "Your appointment on " + appointment.getDateTime() + " was confirmed");
    }

    @AfterReturning(pointcut = "appointmentCancelled(appointment)", argNames = "appointment")
    public void notifyCancelled(Appointment appointment) {
        appointment.notifyPatient(NotificationType.APPOINTMENT_CANCELLED,
                "Your appointment on " + appointment.getDateTime() + " was cancelled");
    }

    @AfterReturning(pointcut = "reminderSent(appointment)", argNames = "appointment")
    public void notifyReminder(Appointment appointment) {
        appointment.notifyPatient(NotificationType.APPOINTMENT_REMINDER,
                "Reminder: you have an appointment on " + appointment.getDateTime());
    }
}
