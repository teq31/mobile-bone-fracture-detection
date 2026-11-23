package com.bonefracture.analysis;

import com.bonefracture.detection.AnalysisResult;
import com.bonefracture.persistence.AnalysisEntity;
import com.bonefracture.persistence.UserEntity;
import com.bonefracture.persistence.UserRepository;
import com.bonefracture.service.ModelComparisonService;
import com.bonefracture.user.Role;
import com.bonefracture.xray.BodyPart;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalysisStorageServiceTest {

    // Tomcat resolves a relative path in transferTo(File) against its own temp folder, not the app folder
    private static class ServletUpload extends MockMultipartFile {
        ServletUpload() {
            super("image", "wrist.png", "image/png", new byte[]{1, 2, 3});
        }

        @Override
        public void transferTo(File dest) throws IOException {
            if (!dest.isAbsolute()) {
                throw new IOException("relative path given to the servlet container: " + dest);
            }
            super.transferTo(dest);
        }
    }

    @Test
    void uploadIsSavedWithAnAbsolutePathWhenTheFolderIsRelative() {
        UserRepository users = mock(UserRepository.class);
        when(users.findById(1L)).thenReturn(Optional.of(new UserEntity("Ion", "ion@mail.com", "hash", Role.PATIENT)));
        XRayAnalysisService analysis = mock(XRayAnalysisService.class);
        when(analysis.analyzeXRay(any(), any(), any())).thenReturn(new AnalysisResult(null));
        AnalysisHistoryService history = mock(AnalysisHistoryService.class);
        when(history.save(any())).thenAnswer(call -> call.getArgument(0));
        AnalysisStorageService storage = new AnalysisStorageService(analysis, mock(ModelComparisonService.class),
                history, users, "target/test-uploads");

        AnalysisEntity saved = storage.analyze(1L, new ServletUpload(), BodyPart.WRIST);

        Path image = Path.of(saved.getImagePath());
        assertTrue(image.isAbsolute(), saved.getImagePath());
        assertTrue(image.toFile().isFile());
        assertTrue(image.startsWith(Path.of("target/test-uploads").toAbsolutePath()));
    }
}
