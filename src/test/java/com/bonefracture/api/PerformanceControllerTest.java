package com.bonefracture.api;

import com.bonefracture.performance.PerformanceMonitor;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// timings from PerformanceMonitoringAspect through the api, admin only
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PerformanceControllerTest {

    private static final long MS = 1_000_000L;

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
        PerformanceMonitor.reset();
        userService.register(Role.ADMINISTRATOR, "Admin", "admin@mail.com", "admin1234");
        userService.register(Role.PATIENT, "Ion", "ion@mail.com", "pass1234");
        adminToken = bearer("admin@mail.com");
        patientToken = bearer("ion@mail.com");
    }

    private String bearer(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow();
        return "Bearer " + tokenService.createToken(user);
    }

    @Test
    void adminSeesTheSlowestStepsFirst() throws Exception {
        PerformanceMonitor.record("ImagePreprocessor.resize", 20 * MS, false);
        PerformanceMonitor.record("YoloOnnxModel.detect", 150 * MS, false);
        PerformanceMonitor.record("YoloOnnxModel.detect", 170 * MS, true);

        mockMvc.perform(get("/api/performance/stats").header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].operation").value("YoloOnnxModel.detect"))
                .andExpect(jsonPath("$[0].calls").value(2))
                .andExpect(jsonPath("$[0].failures").value(1))
                .andExpect(jsonPath("$[0].averageMillis").value(160.0))
                .andExpect(jsonPath("$[0].maxMillis").value(170.0))
                .andExpect(jsonPath("$[1].operation").value("ImagePreprocessor.resize"));
    }

    @Test
    void adminCanResetTheStatistics() throws Exception {
        PerformanceMonitor.record("YoloOnnxModel.detect", 150 * MS, false);

        mockMvc.perform(delete("/api/performance/stats").header("Authorization", adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/performance/stats").header("Authorization", adminToken))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void otherRolesAreRefused() throws Exception {
        mockMvc.perform(get("/api/performance/stats").header("Authorization", patientToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/performance/stats").header("Authorization", patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void aTokenIsRequired() throws Exception {
        mockMvc.perform(get("/api/performance/stats")).andExpect(status().isUnauthorized());
    }
}
