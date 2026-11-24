package com.bonefracture.api;

import com.bonefracture.persistence.UserEntity;
import com.bonefracture.persistence.UserRepository;
import com.bonefracture.security.TokenService;
import com.bonefracture.user.Role;
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
 * Spring Boot Actuator: GET /actuator/health is public (Docker, Azure and the demo check it)
 * and shows only UP / DOWN. The other Actuator endpoints are not exposed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HealthCheckTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Test
    void healthIsUpWithoutAToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void healthShowsNoDetails() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(jsonPath("$.components").doesNotExist())
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void livenessAndReadinessAreUp() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void otherActuatorEndpointsAreNotPublic() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void otherActuatorEndpointsAreNotExposedEvenWithAToken() throws Exception {
        UserEntity admin = userRepository.save(new UserEntity("Admin", "admin@mail.com", "hash", Role.ADMINISTRATOR));
        String token = "Bearer " + tokenService.createToken(admin);

        mockMvc.perform(get("/actuator/env").header("Authorization", token))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/actuator/beans").header("Authorization", token))
                .andExpect(status().isNotFound());
    }
}
