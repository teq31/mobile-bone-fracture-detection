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
 * NotificationAspect (AOP) + DatabaseNotificationObserver: an appointment change through the API
 * creates a notification for the patient, which the app reads from /api/notifications.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserEntity doctor;
    private UserEntity patient;
    private long appointmentId;

    @BeforeEach
    void setUp() throws Exception {
        doctor = userRepository.save(new UserEntity("Dr. Smith", "smith@clinic.com", "hash", Role.DOCTOR));
        patient = userRepository.save(new UserEntity("John Doe", "john@mail.com", "hash", Role.PATIENT));
        String body = """
                {"doctorId": %d, "dateTime": "%s"}
                """.formatted(doctor.getId(), LocalDateTime.now().plusDays(3).withNano(0));
        String response = mockMvc.perform(post("/api/appointments").header("Authorization", bearer(patient))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        appointmentId = objectMapper.readTree(response).get("id").asLong();
    }

    private String bearer(UserEntity user) {
        return "Bearer " + tokenService.createToken(user);
    }

    private ResultActions action(UserEntity user, String action) throws Exception {
        return mockMvc.perform(post("/api/appointments/{id}/{action}", appointmentId, action)
                .header("Authorization", bearer(user)));
    }

    private ResultActions notificationsOf(UserEntity user) throws Exception {
        return mockMvc.perform(get("/api/notifications").header("Authorization", bearer(user)));
    }

    @Test
    void confirmingCreatesNotificationForThePatientOnly() throws Exception {
        action(doctor, "confirm").andExpect(status().isOk());

        notificationsOf(patient)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("APPOINTMENT_CONFIRMED"))
                .andExpect(jsonPath("$[0].read").value(false));
        notificationsOf(doctor).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void everyChangeCreatesANotificationWithTheRightType() throws Exception {
        action(doctor, "confirm");
        action(doctor, "reminder");
        action(patient, "cancel");

        notificationsOf(patient)
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[*].type").value(org.hamcrest.Matchers.containsInAnyOrder(
                        "APPOINTMENT_CONFIRMED", "APPOINTMENT_REMINDER", "APPOINTMENT_CANCELLED")));
    }

    @Test
    void actionNotAllowedCreatesNoNotification() throws Exception {
        action(doctor, "reminder").andExpect(status().isConflict());

        notificationsOf(patient).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void recipientMarksNotificationAsReadAndOthersCannot() throws Exception {
        action(doctor, "confirm");
        String response = notificationsOf(patient).andReturn().getResponse().getContentAsString();
        long notificationId = objectMapper.readTree(response).get(0).get("id").asLong();

        mockMvc.perform(post("/api/notifications/{id}/read", notificationId).header("Authorization", bearer(doctor)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/notifications/{id}/read", notificationId).header("Authorization", bearer(patient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
        mockMvc.perform(post("/api/notifications/{id}/read", 999_999).header("Authorization", bearer(patient)))
                .andExpect(status().isNotFound());
    }
}
