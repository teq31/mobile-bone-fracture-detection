package com.bonefracture.api;

import com.bonefracture.persistence.UserEntity;
import com.bonefracture.persistence.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Register and login through the REST API, with the real database (H2) and security.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private ResultActions register(String email, String password, String role) throws Exception {
        String body = """
                {"name": "John Doe", "email": "%s", "password": "%s", "role": "%s"}
                """.formatted(email, password, role);
        return mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions login(String email, String password) throws Exception {
        String body = """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void registerCreatesAccountWithHashedPassword() throws Exception {
        register("john@mail.com", "secret123", "PATIENT")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john@mail.com"))
                .andExpect(jsonPath("$.role").value("PATIENT"))
                .andExpect(jsonPath("$.password").doesNotExist());

        UserEntity saved = userRepository.findByEmail("john@mail.com").orElseThrow();
        assertNotEquals("secret123", saved.getPasswordHash());
        assertTrue(passwordEncoder.matches("secret123", saved.getPasswordHash()));
    }

    @Test
    void newDoctorIsNotVerified() throws Exception {
        register("doctor@clinic.com", "secret123", "DOCTOR")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verified").value(false));

        assertFalse(userRepository.findByEmail("doctor@clinic.com").orElseThrow().isVerified());
    }

    @Test
    void registerWithUsedEmailReturnsConflict() throws Exception {
        register("john@mail.com", "secret123", "PATIENT").andExpect(status().isCreated());

        register("john@mail.com", "other1234", "PATIENT").andExpect(status().isConflict());
    }

    @Test
    void registerWithInvalidDataReturnsBadRequest() throws Exception {
        register("not-an-email", "secret123", "PATIENT").andExpect(status().isBadRequest());
        register("john@mail.com", "short", "PATIENT").andExpect(status().isBadRequest());
    }

    @Test
    void administratorCannotRegister() throws Exception {
        register("admin@mail.com", "secret123", "ADMINISTRATOR").andExpect(status().isBadRequest());
    }

    @Test
    void loginReturnsTokenThatOpensProtectedEndpoints() throws Exception {
        register("john@mail.com", "secret123", "PATIENT");

        String response = login("john@mail.com", "secret123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        String token = json.get("token").asText();

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@mail.com"));
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        register("john@mail.com", "secret123", "PATIENT");

        login("john@mail.com", "wrong-password").andExpect(status().isUnauthorized());
        login("nobody@mail.com", "secret123").andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }
}
