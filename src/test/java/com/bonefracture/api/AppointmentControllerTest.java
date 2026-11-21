package com.bonefracture.api;

import com.bonefracture.persistence.UserEntity;
import com.bonefracture.persistence.UserRepository;
import com.bonefracture.security.TokenService;
import com.bonefracture.user.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Appointments through the REST API: the STATE rules, plus who may do what.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserEntity doctor;
    private UserEntity otherDoctor;
    private UserEntity patient;
    private UserEntity otherPatient;

    @BeforeEach
    void setUp() {
        doctor = userRepository.save(new UserEntity("Dr. Smith", "smith@clinic.com", "hash", Role.DOCTOR));
        otherDoctor = userRepository.save(new UserEntity("Dr. Brown", "brown@clinic.com", "hash", Role.DOCTOR));
        patient = userRepository.save(new UserEntity("John Doe", "john@mail.com", "hash", Role.PATIENT));
        otherPatient = userRepository.save(new UserEntity("Anna Lee", "anna@mail.com", "hash", Role.PATIENT));
    }

    private String bearer(UserEntity user) {
        return "Bearer " + tokenService.createToken(user);
    }

    private ResultActions requestAppointment(UserEntity user, Long doctorId) throws Exception {
        String body = """
                {"doctorId": %d, "dateTime": "%s"}
                """.formatted(doctorId, LocalDateTime.now().plusDays(3).withNano(0));
        return mockMvc.perform(post("/api/appointments").header("Authorization", bearer(user))
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private long createAppointment() throws Exception {
        String response = requestAppointment(patient, doctor.getId())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private ResultActions action(UserEntity user, long appointmentId, String action) throws Exception {
        return mockMvc.perform(post("/api/appointments/{id}/{action}", appointmentId, action)
                .header("Authorization", bearer(user)));
    }

    @Test
    void patientRequestsAppointment() throws Exception {
        requestAppointment(patient, doctor.getId())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.doctorName").value("Dr. Smith"))
                .andExpect(jsonPath("$.patientName").value("John Doe"));
    }

    @Test
    void onlyPatientsCanRequestAndOnlyWithADoctor() throws Exception {
        requestAppointment(doctor, otherDoctor.getId()).andExpect(status().isForbidden());
        requestAppointment(patient, otherPatient.getId()).andExpect(status().isBadRequest());
    }

    @Test
    void doctorConfirmsAndThenCanSendReminder() throws Exception {
        long id = createAppointment();

        action(doctor, id, "confirm").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));
        action(doctor, id, "reminder").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void onlyTheDoctorOfTheAppointmentCanConfirm() throws Exception {
        long id = createAppointment();

        action(patient, id, "confirm").andExpect(status().isForbidden());
        action(otherDoctor, id, "confirm").andExpect(status().isForbidden());
    }

    @Test
    void actionNotAllowedInCurrentStateReturnsConflict() throws Exception {
        long id = createAppointment();

        action(doctor, id, "reminder").andExpect(status().isConflict());
        action(doctor, id, "confirm").andExpect(status().isOk());
        action(doctor, id, "confirm").andExpect(status().isConflict());
    }

    @Test
    void patientCancelsAndCancelledAppointmentAllowsNothing() throws Exception {
        long id = createAppointment();

        action(patient, id, "cancel").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
        action(doctor, id, "confirm").andExpect(status().isConflict());
        action(otherPatient, id, "cancel").andExpect(status().isForbidden());
    }

    @Test
    void usersSeeOnlyTheirOwnAppointments() throws Exception {
        long id = createAppointment();

        mockMvc.perform(get("/api/appointments").header("Authorization", bearer(patient)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/appointments").header("Authorization", bearer(doctor)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/appointments").header("Authorization", bearer(otherPatient)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/appointments/{id}", id).header("Authorization", bearer(otherPatient)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unknownAppointmentReturnsNotFoundAndNoTokenReturnsUnauthorized() throws Exception {
        action(doctor, 999_999, "confirm").andExpect(status().isNotFound());
        mockMvc.perform(get("/api/appointments")).andExpect(status().isUnauthorized());
    }
}
