package com.bonefracture.aspect;

import com.bonefracture.analysis.FractureAnalysisFacade;
import com.bonefracture.analysis.XRayAnalysisService;
import com.bonefracture.detection.FractureDetector;
import com.bonefracture.detection.HeatmapGenerator;
import com.bonefracture.detection.ModelRegistry;
import com.bonefracture.detection.YoloModel;
import com.bonefracture.history.HistoryRepository;
import com.bonefracture.performance.OperationStats;
import com.bonefracture.performance.PerformanceMonitor;
import com.bonefracture.user.Student;
import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.ImagePreprocessor;
import com.bonefracture.xray.adapter.ImageFileAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lab 6/7 - PerformanceMonitoringAspect (AOP): every step of an analysis is timed without a single line of
 * timing code in the facade, the adapters, the preprocessor, the models or the heatmap generator.
 * Only real (main) classes are used, because the aspect is woven into them at compile time.
 */
class PerformanceMonitoringAspectTest {

    private XRayAnalysisService service;
    private File folder;
    private final Student student = new Student("Ana Pop", "ana@student.com");

    @BeforeEach
    void setUp() throws IOException {
        PerformanceMonitor.reset();
        folder = Files.createTempDirectory("performance").toFile();

        YoloModel yolo = new YoloModel("v8", "yolov8.onnx");
        yolo.setMain(true);
        ModelRegistry models = new ModelRegistry();
        models.register(yolo);
        service = new FractureAnalysisFacade(new ImagePreprocessor(), models, new FractureDetector(yolo),
                new HeatmapGenerator(), new HistoryRepository());
    }

    // a different picture in every test, so a cache in front of the model never answers instead of it
    private File xrayFile() throws IOException {
        BufferedImage pixels = new BufferedImage(320, 240, BufferedImage.TYPE_BYTE_GRAY);
        pixels.getRaster().setSample(0, 0, 0, (int) (System.nanoTime() % 256));
        pixels.getRaster().setSample(1, 0, 0, (int) (Math.random() * 256));
        File file = File.createTempFile("wrist", ".png", folder);
        ImageIO.write(pixels, "png", file);
        return file;
    }

    private OperationStats stats(String operation) {
        return PerformanceMonitor.get(operation)
                .orElseThrow(() -> new AssertionError("not monitored: " + operation + " in " + PerformanceMonitor.all()));
    }

    @Test
    void everyStepOfAnAnalysisIsTimed() throws IOException {
        service.analyzeXRay(student, new ImageFileAdapter(xrayFile(), FileFormat.PNG), BodyPart.WRIST);

        assertEquals(1, stats("FractureAnalysisFacade.analyzeXRay").calls());
        assertEquals(1, stats("ImageFileAdapter.toXRayImage").calls());
        assertEquals(1, stats("ImagePreprocessor.resize").calls());
        assertEquals(1, stats("ImagePreprocessor.normalize").calls());
        assertEquals(1, stats("YoloModel.detect").calls());
    }

    @Test
    void theWholeAnalysisTakesAtLeastAsLongAsItsSteps() throws IOException {
        service.analyzeXRay(student, new ImageFileAdapter(xrayFile(), FileFormat.PNG), BodyPart.WRIST);

        double whole = stats("FractureAnalysisFacade.analyzeXRay").lastMillis();
        double steps = stats("ImageFileAdapter.toXRayImage").lastMillis()
                + stats("ImagePreprocessor.resize").lastMillis()
                + stats("ImagePreprocessor.normalize").lastMillis();
        assertTrue(whole >= steps, whole + " ms < " + steps + " ms");
    }

    @Test
    void failedCallsAreCountedAndTheExceptionIsNotSwallowed() {
        ImageFileAdapter missing = new ImageFileAdapter(new File(folder, "missing.png"), FileFormat.PNG);

        assertThrows(IllegalArgumentException.class, () -> service.analyzeXRay(student, missing, BodyPart.WRIST));

        assertEquals(1, stats("FractureAnalysisFacade.analyzeXRay").failures());
        assertEquals(1, stats("ImageFileAdapter.toXRayImage").failures());
    }

    @Test
    void historyCallsAreTimedToo() {
        service.getHistory(student);
        service.getHistory(student);

        assertEquals(2, stats("FractureAnalysisFacade.getHistory").calls());
    }
}
