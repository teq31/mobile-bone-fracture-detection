package com.bonefracture.api.config;

import com.bonefracture.detection.ModelRegistry;
import com.bonefracture.detection.YoloModel;
import com.bonefracture.detection.YoloOnnxModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisBeansConfigTest {

    private final AnalysisBeansConfig config = new AnalysisBeansConfig();

    @Test
    void onnxModelIsTheMainModelWhenItsFileExists(@TempDir Path dir) throws IOException {
        Path file = Files.createFile(dir.resolve("yolo.onnx"));
        ModelRegistry registry = config.modelRegistry(file.toString(), "v8", "rfdetr.onnx", "r50", "resnet50");
        assertInstanceOf(YoloOnnxModel.class, registry.getMainModel());
        assertEquals(2, registry.getAllModels().size());
    }

    @Test
    void placeholderModelIsUsedWhenTheFileIsMissing(@TempDir Path dir) {
        ModelRegistry registry = config.modelRegistry(dir.resolve("missing.onnx").toString(), "v8",
                "rfdetr.onnx", "r50", "resnet50");
        assertInstanceOf(YoloModel.class, registry.getMainModel());
    }
}
