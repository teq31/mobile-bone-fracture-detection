package com.bonefracture.api;

import com.bonefracture.persistence.UserEntity;
import com.bonefracture.persistence.UserRepository;
import com.bonefracture.security.TokenService;
import com.bonefracture.user.Role;
import com.bonefracture.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET /api/doctors: the verified doctors a patient can choose for an appointment.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    private String patientToken;

    @BeforeEach
    void setUp() {
        userService.register(Role.PATIENT, "Ion", "ion@mail.com", "pass1234");
        userService.register(Role.DOCTOR, "Maria Pop", "maria@clinic.com", "pass1234");
        userService.register(Role.DOCTOR, "Andrei Ionescu", "andrei@clinic.com", "pass1234");
        userService.register(Role.DOCTOR, "Dan Unverified", "dan@clinic.com", "pass1234");
        userService.verifyDoctor("maria@clinic.com");
        userService.verifyDoctor("andrei@clinic.com");
        patientToken = bearer("ion@mail.com");
    }

    private String bearer(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow();
        return "Bearer " + tokenService.createToken(user);
    }

    @Test
    void listsOnlyVerifiedDoctorsSortedByName() throws Exception {
        mockMvc.perform(get("/api/doctors").header("Authorization", patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Andrei Ionescu"))
                .andExpect(jsonPath("$[0].email").value("andrei@clinic.com"))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[1].name").value("Maria Pop"));
    }

    @Test
    void doesNotExposePasswordOrRole() throws Exception {
        mockMvc.perform(get("/api/doctors").header("Authorization", patientToken))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void anyLoggedInUserCanSeeTheList() throws Exception {
        mockMvc.perform(get("/api/doctors").header("Authorization", bearer("dan@clinic.com")))
                .andExpect(status().isOk());
    }

    @Test
    void requiresAToken() throws Exception {
        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isUnauthorized());
    }
}
