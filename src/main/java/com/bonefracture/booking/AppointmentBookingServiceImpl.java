package com.bonefracture.booking;

import com.bonefracture.appointment.Appointment;
import com.bonefracture.appointment.AppointmentService;
import com.bonefracture.appointment.AppointmentServiceImpl;
import com.bonefracture.persistence.AppointmentEntity;
import com.bonefracture.persistence.AppointmentRepository;
import com.bonefracture.persistence.UserEntity;
import com.bonefracture.persistence.UserRepository;
import com.bonefracture.user.Doctor;
import com.bonefracture.user.Patient;
import com.bonefracture.user.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * SOA: implementation of AppointmentBookingService.
 * Loads the appointment, lets the domain (AppointmentService + STATE classes) decide
 * if the action is allowed, then saves the new status.
 */
@Service
@Transactional
public class AppointmentBookingServiceImpl implements AppointmentBookingService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final AppointmentService appointmentService = new AppointmentServiceImpl();

    public AppointmentBookingServiceImpl(AppointmentRepository appointmentRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AppointmentEntity request(Long patientId, Long doctorId, LocalDateTime dateTime) {
        UserEntity patient = findUser(patientId);
        if (patient.getRole() != Role.PATIENT) {
            throw new NotAllowedException("Only patients can request appointments");
        }
        UserEntity doctor = findUser(doctorId);
        if (doctor.getRole() != Role.DOCTOR) {
            throw new IllegalArgumentException("The chosen user is not a doctor");
        }
        return appointmentRepository.save(new AppointmentEntity(doctor, patient, dateTime));
    }

    @Override
    public AppointmentEntity confirm(Long appointmentId, Long userId) {
        AppointmentEntity entity = findAppointment(appointmentId);
        requireDoctor(entity, userId);
        return apply(entity, appointmentService::confirm);
    }

    @Override
    public AppointmentEntity cancel(Long appointmentId, Long userId) {
        AppointmentEntity entity = findAppointment(appointmentId);
        requireParticipant(entity, userId);
        return apply(entity, appointmentService::cancel);
    }

    @Override
    public AppointmentEntity sendReminder(Long appointmentId, Long userId) {
        AppointmentEntity entity = findAppointment(appointmentId);
        requireDoctor(entity, userId);
        return apply(entity, appointmentService::sendReminder);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentEntity get(Long appointmentId, Long userId) {
        AppointmentEntity entity = findAppointment(appointmentId);
        requireParticipant(entity, userId);
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentEntity> findForUser(Long userId) {
        UserEntity user = findUser(userId);
        if (user.getRole() == Role.DOCTOR) {
            return appointmentRepository.findByDoctorIdOrderByDateTime(userId);
        }
        return appointmentRepository.findByPatientIdOrderByDateTime(userId);
    }

    /** Runs the action on the domain appointment (STATE pattern) and saves the resulting status. */
    private AppointmentEntity apply(AppointmentEntity entity, Consumer<Appointment> action) {
        Appointment appointment = Appointment.restore(toDoctor(entity.getDoctor()), toPatient(entity.getPatient()),
                entity.getDateTime(), entity.getStatus());
        action.accept(appointment);
        entity.setStatus(appointment.getStatus());
        return entity;
    }

    private void requireDoctor(AppointmentEntity entity, Long userId) {
        if (!entity.getDoctor().getId().equals(userId)) {
            throw new NotAllowedException("Only the doctor of this appointment can do this");
        }
    }

    private void requireParticipant(AppointmentEntity entity, Long userId) {
        if (!entity.getDoctor().getId().equals(userId) && !entity.getPatient().getId().equals(userId)) {
            throw new NotAllowedException("This appointment belongs to other users");
        }
    }

    private AppointmentEntity findAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
    }

    private UserEntity findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown user " + userId));
    }

    private Doctor toDoctor(UserEntity user) {
        return new Doctor(user.getName(), user.getEmail());
    }

    private Patient toPatient(UserEntity user) {
        return new Patient(user.getName(), user.getEmail());
    }
}
