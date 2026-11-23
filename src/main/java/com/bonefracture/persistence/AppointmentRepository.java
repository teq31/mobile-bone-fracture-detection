package com.bonefracture.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {

    List<AppointmentEntity> findByPatientIdOrderByDateTime(Long patientId);

    List<AppointmentEntity> findByDoctorIdOrderByDateTime(Long doctorId);
}
