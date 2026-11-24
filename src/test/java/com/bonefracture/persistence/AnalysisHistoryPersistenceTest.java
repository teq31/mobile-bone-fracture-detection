package com.bonefracture.persistence;

import com.bonefracture.analysis.AnalysisHistoryService;
import com.bonefracture.analysis.JpaAnalysisHistoryService;
import com.bonefracture.user.Role;
import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The analysis history is kept in the database (table analyses), per user id, through
 * AnalysisHistoryService (JpaAnalysisHistoryService). Every check reads back from the database,
 * not from the objects kept in memory.
 */
@DataJpaTest
@Import(JpaAnalysisHistoryService.class)
class AnalysisHistoryPersistenceTest {

    @Autowired
    private AnalysisHistoryService history;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Long patientId;
    private Long otherPatientId;

    @BeforeEach
    void setUp() {
        patientId = userRepository.save(new UserEntity("John Doe", "john@mail.com", "hash", Role.PATIENT)).getId();
        otherPatientId = userRepository.save(new UserEntity("Ana Pop", "ana@mail.com", "hash", Role.PATIENT)).getId();
    }

    private AnalysisEntity analysis(Long userId, String imagePath) {
        return new AnalysisEntity(userId, imagePath, FileFormat.PNG, BodyPart.WRIST, "YOLO v8");
    }

    /** Writes everything to the database and forgets the objects, so the next read is a real query. */
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void analysisWithDetectionsIsReadBackFromTheDatabase() {
        AnalysisEntity analysis = analysis(patientId, "uploads/wrist.png");
        analysis.addDetection(new DetectionEntity("fracture", 0.87f, 10, 20, 30, 40));
        Long id = history.save(analysis).getId();
        flushAndClear();

        AnalysisEntity found = history.findById(id).orElseThrow();

        assertEquals(patientId, found.getUserId());
        assertEquals("uploads/wrist.png", found.getImagePath());
        assertEquals(BodyPart.WRIST, found.getBodyPart());
        assertEquals("YOLO v8", found.getModel());
        assertEquals(1, found.getDetections().size());
        DetectionEntity detection = found.getDetections().get(0);
        assertEquals("fracture", detection.getLabel());
        assertEquals(0.87f, detection.getConfidence(), 0.0001f);
        assertEquals(30, detection.getBoxWidth());
    }

    @Test
    void historyContainsOnlyTheUsersAnalysesNewestFirst() throws InterruptedException {
        history.save(analysis(patientId, "uploads/first.png"));
        Thread.sleep(10); // different createdAt
        history.save(analysis(otherPatientId, "uploads/other.png"));
        Thread.sleep(10);
        history.save(analysis(patientId, "uploads/second.png"));
        flushAndClear();

        List<AnalysisEntity> found = history.findByUser(patientId);

        assertEquals(List.of("uploads/second.png", "uploads/first.png"),
                found.stream().map(AnalysisEntity::getImagePath).toList());
    }

    @Test
    void userWithoutAnalysesHasEmptyHistory() {
        history.save(analysis(otherPatientId, "uploads/other.png"));
        flushAndClear();

        assertTrue(history.findByUser(patientId).isEmpty());
    }

    @Test
    void unknownAnalysisIsNotFound() {
        assertTrue(history.findById(999_999L).isEmpty());
    }
}
