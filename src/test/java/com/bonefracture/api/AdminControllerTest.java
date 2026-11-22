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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    private String adminToken;
    private String patientToken;

    @BeforeEach
    void setUp() {
        userService.register(Role.ADMINISTRATOR, "Admin", "admin@mail.com", "admin1234");
        userService.register(Role.PATIENT, "Ion", "ion@mail.com", "pass1234");
        userService.register(Role.DOCTOR, "Dan", "dan@clinic.com", "pass1234");
        adminToken = bearer("admin@mail.com");
        patientToken = bearer("ion@mail.com");
    }

    private String bearer(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow();
        return "Bearer " + tokenService.createToken(user);
    }

    private ResultActions verify(String token, String email) throws Exception {
        return mockMvc.perform(post("/api/admin/doctors/verify")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + email + "\"}"));
    }

    @Test
    void adminSeesUnverifiedDoctors() throws Exception {
        mockMvc.perform(get("/api/admin/doctors/unverified").header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("dan@clinic.com"));
    }

    @Test
    void adminVerifiesDoctor() throws Exception {
        verify(adminToken, "dan@clinic.com")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));
        assertTrue(userRepository.findByEmail("dan@clinic.com").orElseThrow().isVerified());
        mockMvc.perform(get("/api/admin/doctors/unverified").header("Authorization", adminToken))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void otherRolesAreRefused() throws Exception {
        verify(patientToken, "dan@clinic.com").andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/doctors/unverified").header("Authorization", patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void unknownEmailIsNotFound() throws Exception {
        verify(adminToken, "nobody@mail.com").andExpect(status().isNotFound());
    }

    @Test
    void onlyDoctorsCanBeVerified() throws Exception {
        verify(adminToken, "ion@mail.com").andExpect(status().isBadRequest());
    }
}
