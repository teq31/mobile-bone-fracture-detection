package com.bonefracture.cache;

import com.bonefracture.detection.AnalysisResult;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * CachingAspect on the detection models: the same X-ray (same file content) does not go through
 * the same model twice. The model classes contain no caching code; the aspect is woven at compile time.
 */
class CachingAspectTest {

    @TempDir
    Path folder;

    private final YoloModel yolo = new YoloModel("v8", "yolov8.onnx");
    private final RfDetrModel rfDetr = new RfDetrModel("1.0", "rfdetr.onnx", "resnet50");

    @BeforeEach
    void setUp() {
        CacheRegistry.clearAll();
    }

    private XRayImage image(String fileName, String content) throws IOException {
        Path file = folder.resolve(fileName);
        Files.writeString(file, content);
        return new XRayImage(file.toString(), FileFormat.PNG, BodyPart.WRIST, false);
    }

    private CacheStats analysisStats() {
        return CacheRegistry.get(CacheRegistry.ANALYSIS).stats();
    }

    @Test
    void sameImageIsAnalyzedOnlyOnceByTheSameModel() throws IOException {
        XRayImage xray = image("wrist.png", "pixels of the X-ray");

        AnalysisResult first = yolo.detect(xray);
        AnalysisResult second = yolo.detect(xray);

        assertSame(first, second);
        assertEquals(1, analysisStats().misses());
        assertEquals(1, analysisStats().hits());
    }

    @Test
    void sameContentUnderAnotherFileNameIsAHit() throws IOException {
        AnalysisResult first = yolo.detect(image("upload-1.png", "same pixels"));
        AnalysisResult second = yolo.detect(image("upload-2.png", "same pixels"));

        assertSame(first, second);
        assertEquals(1, analysisStats().hits());
    }

    @Test
    void differentImageIsAnalyzedAgain() throws IOException {
        AnalysisResult first = yolo.detect(image("a.png", "first X-ray"));
        AnalysisResult second = yolo.detect(image("b.png", "second X-ray"));

        assertNotSame(first, second);
        assertEquals(2, analysisStats().misses());
        assertEquals(0, analysisStats().hits());
    }

    @Test
    void eachModelHasItsOwnResultForModelComparison() throws IOException {
        XRayImage xray = image("wrist.png", "pixels");

        AnalysisResult byYolo = yolo.detect(xray);
        AnalysisResult byRfDetr = rfDetr.detect(xray);

        assertSame(yolo, byYolo.getModel());
        assertSame(rfDetr, byRfDetr.getModel());
        assertSame(byRfDetr, rfDetr.detect(xray));
        assertEquals(2, analysisStats().size());
        assertEquals(1, analysisStats().hits());
    }

    @Test
    void imageWithoutReadableFileIsNotCached() {
        XRayImage missingFile = new XRayImage("does-not-exist.png", FileFormat.PNG, BodyPart.WRIST, false);

        AnalysisResult first = yolo.detect(missingFile);
        AnalysisResult second = yolo.detect(missingFile);

        assertNotSame(first, second);
        assertEquals(0, analysisStats().size());
        assertEquals(0, analysisStats().hits() + analysisStats().misses());
    }
}
