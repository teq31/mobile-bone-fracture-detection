package com.bonefracture.api;

import com.bonefracture.persistence.UserEntity;
import com.bonefracture.persistence.UserRepository;
import com.bonefracture.security.TokenService;
import com.bonefracture.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    private UserEntity patient;

    @BeforeEach
    void setUp() {
        patient = userRepository.save(new UserEntity("John Doe", "john@mail.com", "hash", Role.PATIENT));
    }

    private String bearer(UserEntity user) {
        return "Bearer " + tokenService.createToken(user);
    }

    private MockMultipartFile pngUpload() throws Exception {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_BYTE_GRAY);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return new MockMultipartFile("image", "xray.png", "image/png", out.toByteArray());
    }

    private long createAnalysis() throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/analyses")
                        .file(pngUpload())
                        .param("bodyPart", "WRIST")
                        .header("Authorization", bearer(patient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.analysisId").exists())
                .andExpect(jsonPath("$.imageUrl").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn();
        return com.jayway.jsonpath.JsonPath.parse(result.getResponse().getContentAsString())
                .read("$.analysisId", Long.class);
    }

    @Test
    void analyze_storesAnalysisAndReturnsIt() throws Exception {
        createAnalysis();
    }

    @Test
    void analyze_rejectsMissingBodyPart() throws Exception {
        mockMvc.perform(multipart("/api/analyses")
                        .file(pngUpload())
                        .header("Authorization", bearer(patient)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void history_listsTheUsersAnalyses() throws Exception {
        createAnalysis();
        mockMvc.perform(get("/api/analyses/history").header("Authorization", bearer(patient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].analysisId").exists())
                .andExpect(jsonPath("$.items[0].imageUrl").exists());
    }

    @Test
    void get_returnsTheAnalysisById() throws Exception {
        long id = createAnalysis();
        mockMvc.perform(get("/api/analyses/" + id).header("Authorization", bearer(patient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value((int) id));
    }

    @Test
    void get_returnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/analyses/999999").header("Authorization", bearer(patient)))
                .andExpect(status().isNotFound());
    }

    @Test
    void image_returnsTheStoredImageBytes() throws Exception {
        long id = createAnalysis();
        mockMvc.perform(get("/api/analyses/" + id + "/image").header("Authorization", bearer(patient)))
                .andExpect(status().isOk());
    }

    @Test
    void endpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/analyses/history"))
                .andExpect(status().isUnauthorized());
    }
}
