package com.bonefracture.api.config;

import com.bonefracture.detection.DetectionModel;
import com.bonefracture.detection.FractureDetector;
import com.bonefracture.detection.HeatmapGenerator;
import com.bonefracture.detection.ModelRegistry;
import com.bonefracture.detection.YoloModel;
import com.bonefracture.detection.YoloOnnxModel;
import com.bonefracture.detection.RfDetrModel;
import com.bonefracture.history.HistoryRepository;
import com.bonefracture.analysis.FractureAnalysisFacade;
import com.bonefracture.service.ConfigurationService;
import com.bonefracture.service.ModelComparisonService;
import com.bonefracture.xray.ImagePreprocessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnalysisBeansConfig {

    @Bean
    public ConfigurationService configurationService() {
        return new ConfigurationService();
    }

    @Bean
    public ImagePreprocessor imagePreprocessor() {
        return new ImagePreprocessor();
    }

    @Bean
    public HeatmapGenerator heatmapGenerator() {
        return new HeatmapGenerator();
    }

    @Bean
    public HistoryRepository historyRepository() {
        return new HistoryRepository();
    }

    @Bean
    public ModelRegistry modelRegistry(@Value("${app.analysis.yolo-weights:models/yolov8-fracture.onnx}") String yoloWeights,
                                       @Value("${app.analysis.yolo-version:v8}") String yoloVersion,
                                       @Value("${app.analysis.rfdetr-weights:models/rfdetr.onnx}") String rfDetrWeights,
                                       @Value("${app.analysis.rfdetr-version:r50}") String rfDetrVersion,
                                       @Value("${app.analysis.rfdetr-backbone:resnet50}") String rfDetrBackbone) {
        ModelRegistry registry = new ModelRegistry();
        // the real model once ml/export_onnx.py has created the file, otherwise the placeholder
        YoloOnnxModel onnx = new YoloOnnxModel(yoloVersion, yoloWeights);
        DetectionModel yolo = onnx.isAvailable() ? onnx : new YoloModel(yoloVersion, yoloWeights);
        yolo.setMain(true);
        registry.register(yolo);
        registry.register(new RfDetrModel(rfDetrVersion, rfDetrWeights, rfDetrBackbone));
        return registry;
    }

    @Bean
    public FractureDetector fractureDetector(ModelRegistry modelRegistry) {
        return new FractureDetector(modelRegistry);
    }

    @Bean
    public ModelComparisonService modelComparisonService(ModelRegistry modelRegistry,
                                                          @Value("${app.analysis.min-iou:0.5}") float minIou) {
        return new ModelComparisonService(modelRegistry, minIou);
    }

    @Bean
    public FractureAnalysisFacade fractureAnalysisFacade(ImagePreprocessor imagePreprocessor,
                                                         ModelRegistry modelRegistry,
                                                         FractureDetector fractureDetector,
                                                         HeatmapGenerator heatmapGenerator,
                                                         HistoryRepository historyRepository) {
        return new FractureAnalysisFacade(imagePreprocessor, modelRegistry, fractureDetector,
                heatmapGenerator, historyRepository);
    }
}
