package com.bonefracture.cache;

import com.bonefracture.analysis.FractureAnalysisFacade;
import com.bonefracture.detection.AnalysisResult;
import com.bonefracture.detection.ModelRegistry;
import com.bonefracture.detection.RfDetrModel;
import com.bonefracture.detection.YoloModel;
import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.XRayImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @Cached on FractureAnalysisFacade.compareModels(image): a doctor comparing the models on the
 * same X-ray (same file content) gets the cached comparison.
 */
class ModelComparisonCacheTest {

    @TempDir
    Path folder;

    private FractureAnalysisFacade facade;

    @BeforeEach
    void setUp() {
        CacheRegistry.clearAll();
        ModelRegistry models = new ModelRegistry();
        models.register(new YoloModel("v8", "yolov8.onnx"));
        models.register(new RfDetrModel("1.0", "rfdetr.onnx", "resnet50"));
        facade = new FractureAnalysisFacade(null, models, null, null, null);
    }

    private XRayImage image(String fileName, String content) throws IOException {
        Path file = folder.resolve(fileName);
        Files.writeString(file, content);
        return new XRayImage(file.toString(), FileFormat.PNG, BodyPart.WRIST, false);
    }

    private CacheStats comparisonStats() {
        return CacheRegistry.get("model-comparison").stats();
    }

    @Test
    void sameXRayIsComparedOnce() throws IOException {
        List<AnalysisResult> first = facade.compareModels(image("wrist.png", "pixels"));
        List<AnalysisResult> second = facade.compareModels(image("wrist-copy.png", "pixels"));

        assertSame(first, second);
        assertEquals(2, first.size());
        assertEquals(1, comparisonStats().misses());
        assertEquals(1, comparisonStats().hits());
        assertEquals(600, comparisonStats().ttlSeconds());
    }

    @Test
    void differentXRayIsComparedAgain() throws IOException {
        List<AnalysisResult> first = facade.compareModels(image("wrist.png", "pixels"));
        List<AnalysisResult> second = facade.compareModels(image("ankle.png", "other pixels"));

        assertNotSame(first, second);
        assertEquals(2, comparisonStats().misses());
        assertEquals(0, comparisonStats().hits());
    }
}
