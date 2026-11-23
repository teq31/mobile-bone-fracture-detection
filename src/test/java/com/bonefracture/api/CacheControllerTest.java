package com.bonefracture.api;

import com.bonefracture.cache.CacheRegistry;
import com.bonefracture.detection.YoloModel;
import com.bonefracture.persistence.UserEntity;
import com.bonefracture.persistence.UserRepository;
import com.bonefracture.security.TokenService;
import com.bonefracture.user.Role;
import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.XRayImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The cache statistics through the API.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CacheControllerTest {

    @TempDir
    Path folder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    private String token;

    @BeforeEach
    void setUp() {
        CacheRegistry.clearAll();
        UserEntity user = userRepository.save(new UserEntity("Dr. Smith", "smith@clinic.com", "hash", Role.DOCTOR));
        token = "Bearer " + tokenService.createToken(user);
    }

    @Test
    void statsShowHitsAndMisses() throws Exception {
        Path file = folder.resolve("wrist.png");
        Files.writeString(file, "pixels");
        XRayImage xray = new XRayImage(file.toString(), FileFormat.PNG, BodyPart.WRIST, false);
        YoloModel yolo = new YoloModel("v8", "yolov8.onnx");
        yolo.detect(xray);
        yolo.detect(xray);

        mockMvc.perform(get("/api/cache/stats").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'analysis')].hits").value(1))
                .andExpect(jsonPath("$[?(@.name == 'analysis')].misses").value(1))
                .andExpect(jsonPath("$[?(@.name == 'analysis')].hitRate").value(0.5))
                .andExpect(jsonPath("$[?(@.name == 'analysis')].ttlSeconds").value(86400));
    }

    @Test
    void clearEmptiesTheCaches() throws Exception {
        mockMvc.perform(delete("/api/cache").header("Authorization", token)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cache/stats").header("Authorization", token))
                .andExpect(jsonPath("$[?(@.name == 'analysis')].size").value(0));
    }

    @Test
    void statsNeedAToken() throws Exception {
        mockMvc.perform(get("/api/cache/stats")).andExpect(status().isUnauthorized());
    }
}
