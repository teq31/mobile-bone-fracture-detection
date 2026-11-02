package com.bonefracture.detection;

import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.XRayImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FractureDetectorTest {

    private YoloModel yolo;
    private RfDetrModel rfDetr;
    private XRayImage xray;

    @BeforeEach
    void setUp() {
        yolo = new YoloModel("v8", "yolov8.pt");
        rfDetr = new RfDetrModel("1.0", "rfdetr.pt", "resnet50");
        xray = new XRayImage("wrist.png", FileFormat.PNG, BodyPart.WRIST, false);
    }

    @Test
    void analyzeUsesTheCurrentModel() {
        FractureDetector detector = new FractureDetector(yolo);
        assertSame(yolo, detector.getModel());
        assertSame(yolo, detector.analyze(xray).getModel());
    }

    @Test
    void setModelChangesTheModel() {
        FractureDetector detector = new FractureDetector(yolo);
        detector.setModel(rfDetr);
        assertSame(rfDetr, detector.getModel());
        assertSame(rfDetr, detector.analyze(xray).getModel());
    }

    @Test
    void resultIsSavedOnTheXRay() {
        FractureDetector detector = new FractureDetector(yolo);
        AnalysisResult first = detector.analyze(xray);
        detector.setModel(rfDetr);
        AnalysisResult second = detector.analyze(xray);
        assertEquals(2, xray.getResults().size());
        assertSame(first, xray.getResults().get(0));
        assertSame(second, xray.getResults().get(1));
    }

    @Test
    void modelCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new FractureDetector((DetectionModel) null));
        FractureDetector detector = new FractureDetector(yolo);
        assertThrows(IllegalArgumentException.class, () -> detector.setModel(null));
    }

    @Test
    void startsWithTheMainModelFromRegistry() {
        ModelRegistry registry = new ModelRegistry();
        rfDetr.setMain(true);
        registry.register(yolo);
        registry.register(rfDetr);
        FractureDetector detector = new FractureDetector(registry);
        assertSame(rfDetr, detector.getModel());
    }
}
