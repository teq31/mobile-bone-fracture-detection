package com.bonefracture.analysis;

import com.bonefracture.detection.AnalysisResult;
import com.bonefracture.detection.Detection;
import com.bonefracture.detection.DetectionModel;
import com.bonefracture.detection.FractureDetector;
import com.bonefracture.detection.Heatmap;
import com.bonefracture.detection.HeatmapGenerator;
import com.bonefracture.detection.ModelRegistry;
import com.bonefracture.history.HistoryRepository;
import com.bonefracture.user.Administrator;
import com.bonefracture.user.Doctor;
import com.bonefracture.user.Patient;
import com.bonefracture.user.Student;
import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.ImagePreprocessor;
import com.bonefracture.xray.XRayImage;
import com.bonefracture.xray.adapter.XRayImageSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// tdd it. 2 - facade + XRayAnalysisService, subsystems are fakes
class XRayAnalysisServiceTest {

    private final List<String> steps = new ArrayList<>();

    private XRayImage image;
    private FakeSource source;
    private FakeModel mainModel;
    private FakeModel secondModel;
    private ModelRegistry registry;
    private FakeHeatmapGenerator heatmapGenerator;
    private XRayAnalysisService service;

    private Student student;
    private Doctor doctor;

    @BeforeEach
    void setUp() throws IOException {
        File file = Files.createTempFile("wrist", ".png").toFile();
        Files.write(file.toPath(), new byte[]{1, 2, 3});
        file.deleteOnExit();
        image = new XRayImage(file.getPath(), FileFormat.PNG, BodyPart.WRIST, false);
        source = new FakeSource(image);

        mainModel = new FakeModel("YOLO", 2, steps);
        mainModel.setMain(true);
        secondModel = new FakeModel("RF-DETR", 1, steps);
        registry = new ModelRegistry();
        registry.register(mainModel);
        registry.register(secondModel);

        heatmapGenerator = new FakeHeatmapGenerator();
        service = createService(registry);

        student = new Student("Ana Pop", "ana@student.com");
        doctor = new Doctor("Dr. Smith", "smith@clinic.com");
    }

    private XRayAnalysisService createService(ModelRegistry models) {
        return new FractureAnalysisFacade(new RecordingPreprocessor(steps), models,
                new FractureDetector(models.getMainModel()), heatmapGenerator, new HistoryRepository());
    }

    @Test
    void analyzeXRayUsesMainModel() {
        AnalysisResult result = service.analyzeXRay(student, source, BodyPart.WRIST);

        assertNotNull(result);
        assertSame(mainModel, result.getModel());
    }

    @Test
    void analyzeXRayConvertsSourceWithChosenBodyPart() {
        AnalysisResult result = service.analyzeXRay(student, source, BodyPart.WRIST);

        assertEquals(BodyPart.WRIST, source.requestedBodyPart);
        assertTrue(image.getResults().contains(result));
    }

    @Test
    void analyzeXRayPreprocessesImageBeforeDetection() {
        service.analyzeXRay(student, source, BodyPart.WRIST);

        assertEquals(List.of("resize", "normalize", "detect YOLO"), steps);
    }

    @Test
    void analyzeXRayAddsHeatmapToEveryDetection() {
        AnalysisResult result = service.analyzeXRay(student, source, BodyPart.WRIST);

        assertEquals(2, result.getDetections().size());
        for (Detection detection : result.getDetections()) {
            assertNotNull(detection.getHeatmap());
        }
        assertEquals(2, heatmapGenerator.calls);
    }

    @Test
    void analyzeXRaySavesXRayInUserHistory() {
        service.analyzeXRay(student, source, BodyPart.WRIST);

        assertEquals(List.of(image), service.getHistory(student));
        assertTrue(service.getHistory(new Patient("Ion Pop", "ion@mail.com")).isEmpty());
    }

    @Test
    void analyzeXRayRejectsInvalidImage() {
        XRayImage missing = new XRayImage("does-not-exist.png", FileFormat.PNG, BodyPart.WRIST, false);

        assertThrows(IllegalArgumentException.class,
                () -> service.analyzeXRay(student, new FakeSource(missing), BodyPart.WRIST));
        assertTrue(steps.isEmpty());
        assertTrue(service.getHistory(student).isEmpty());
    }

    @Test
    void analyzeXRayWithoutMainModelThrows() {
        ModelRegistry noMainModel = new ModelRegistry();
        noMainModel.register(secondModel);
        // getMainModel() throws now (Ianis), so build the detector with another model
        XRayAnalysisService serviceWithoutMain = new FractureAnalysisFacade(new RecordingPreprocessor(steps),
                noMainModel, new FractureDetector(secondModel), heatmapGenerator, new HistoryRepository());

        assertThrows(IllegalStateException.class,
                () -> serviceWithoutMain.analyzeXRay(student, source, BodyPart.WRIST));
    }

    @Test
    void analyzeXRayKeepsResultOnOriginalXRayWhenPreprocessingCreatesNewImage() {
        XRayImage prepared = new XRayImage(image.getPath(), FileFormat.PNG, BodyPart.WRIST, false);
        XRayAnalysisService realisticService = new FractureAnalysisFacade(new CopyingPreprocessor(prepared), registry,
                new FractureDetector(mainModel), heatmapGenerator, new HistoryRepository());

        AnalysisResult result = realisticService.analyzeXRay(student, source, BodyPart.WRIST);

        assertTrue(image.getResults().contains(result), "the X-ray in the history shows the result");
        assertTrue(prepared.getResults().contains(result));
    }

    @Test
    void compareModelsRunsEveryModelForVerifiedDoctor() {
        new Administrator("Admin", "admin@app.com").verifyDoctor(doctor);

        List<AnalysisResult> results = service.compareModels(doctor, source, BodyPart.WRIST);

        assertEquals(2, results.size());
        assertSame(mainModel, results.get(0).getModel());
        assertSame(secondModel, results.get(1).getModel());
        for (AnalysisResult result : results) {
            for (Detection detection : result.getDetections()) {
                assertNotNull(detection.getHeatmap());
            }
        }
        assertEquals(List.of(image), service.getHistory(doctor));
    }

    @Test
    void compareModelsRequiresVerifiedDoctor() {
        assertThrows(IllegalStateException.class,
                () -> service.compareModels(doctor, source, BodyPart.WRIST));
    }

    @Test
    void historyOfUserWithoutXRaysIsEmpty() {
        assertTrue(service.getHistory(student).isEmpty());
    }

    // fakes

    private static class FakeSource implements XRayImageSource {
        private final XRayImage image;
        private BodyPart requestedBodyPart;

        FakeSource(XRayImage image) { this.image = image; }

        @Override
        public XRayImage toXRayImage(BodyPart bodyPart) {
            requestedBodyPart = bodyPart;
            return image;
        }

        @Override
        public FileFormat getFormat() { return image.getFormat(); }

        @Override
        public boolean isTakenWithCamera() { return false; }
    }

    private static class FakeModel extends DetectionModel {
        private final int detections;
        private final List<String> steps;

        FakeModel(String name, int detections, List<String> steps) {
            super(name, "test");
            this.detections = detections;
            this.steps = steps;
        }

        @Override
        public AnalysisResult detect(XRayImage xray) {
            steps.add("detect " + name);
            AnalysisResult result = new AnalysisResult(this);
            for (int i = 0; i < detections; i++) {
                result.addDetection(new Detection("fracture", 0.9f, new Rectangle(10, 10, 50, 50), null));
            }
            return result;
        }
    }

    private static class RecordingPreprocessor extends ImagePreprocessor {
        private final List<String> steps;

        RecordingPreprocessor(List<String> steps) { this.steps = steps; }

        @Override
        public XRayImage resize(XRayImage xray) {
            steps.add("resize");
            return xray;
        }

        @Override
        public XRayImage normalize(XRayImage xray) {
            steps.add("normalize");
            return xray;
        }
    }

    // returns a new image, like the real preprocessor
    private static class CopyingPreprocessor extends ImagePreprocessor {
        private final XRayImage prepared;

        CopyingPreprocessor(XRayImage prepared) { this.prepared = prepared; }

        @Override
        public XRayImage resize(XRayImage xray) { return prepared; }

        @Override
        public XRayImage normalize(XRayImage xray) { return xray; }
    }

    private static class FakeHeatmapGenerator extends HeatmapGenerator {
        private int calls;

        @Override
        public Heatmap generate(XRayImage xray, Detection detection) {
            calls++;
            return new Heatmap(null);
        }
    }
}
