package com.bonefracture.detection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelRegistryTest {

    private ModelRegistry registry;
    private YoloModel yolo;
    private RfDetrModel rfDetr;

    @BeforeEach
    void setUp() {
        registry = new ModelRegistry();
        yolo = new YoloModel("v8", "yolov8.pt");
        rfDetr = new RfDetrModel("1.0", "rfdetr.pt", "resnet50");
    }

    @Test
    void returnsTheMainModel() {
        yolo.setMain(true);
        registry.register(yolo);
        registry.register(rfDetr);
        assertSame(yolo, registry.getMainModel());
    }

    @Test
    void keepsAllModels() {
        registry.register(yolo);
        registry.register(rfDetr);
        assertEquals(2, registry.getAllModels().size());
    }

    @Test
    void noMainModelThrows() {
        registry.register(yolo);
        assertThrows(IllegalStateException.class, () -> registry.getMainModel());
    }

    @Test
    void emptyRegistryThrows() {
        assertThrows(IllegalStateException.class, () -> registry.getMainModel());
    }
}
