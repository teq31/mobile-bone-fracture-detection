package com.bonefracture.analysis;

import com.bonefracture.detection.AnalysisResult;
import com.bonefracture.detection.Detection;
import com.bonefracture.detection.DetectionModel;
import com.bonefracture.detection.FractureDetector;
import com.bonefracture.detection.HeatmapGenerator;
import com.bonefracture.detection.ModelRegistry;
import com.bonefracture.history.HistoryRepository;
import com.bonefracture.user.Doctor;
import com.bonefracture.user.User;
import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.ImagePreprocessor;
import com.bonefracture.xray.XRayImage;
import com.bonefracture.xray.adapter.XRayImageSource;

import java.util.ArrayList;
import java.util.List;

// Facade pattern - Facade
// One simple entry point for the app screens. It hides the steps of an analysis:
// adapting the input (Adapter), validation, preprocessing, choosing the model,
// detection (Strategy, through FractureDetector), heatmap and saving to history.
// SOA: it is the implementation of XRayAnalysisService.
public class FractureAnalysisFacade implements XRayAnalysisService {

    private ImagePreprocessor preprocessor;
    private ModelRegistry modelRegistry;
    private FractureDetector detector;
    private HeatmapGenerator heatmapGenerator;
    private HistoryRepository historyRepository;

    public FractureAnalysisFacade(ImagePreprocessor preprocessor,
                                  ModelRegistry modelRegistry,
                                  FractureDetector detector,
                                  HeatmapGenerator heatmapGenerator,
                                  HistoryRepository historyRepository) {
        this.preprocessor = preprocessor;
        this.modelRegistry = modelRegistry;
        this.detector = detector;
        this.heatmapGenerator = heatmapGenerator;
        this.historyRepository = historyRepository;
    }

    @Override
    public AnalysisResult analyzeXRay(User user, XRayImageSource source, BodyPart bodyPart) {
        XRayImage image = source.toXRayImage(bodyPart);
        if (!image.validate()) {
            throw new IllegalArgumentException("The X-ray image is not valid: " + image.getPath());
        }
        DetectionModel mainModel = modelRegistry.getMainModel();
        if (mainModel == null) {
            throw new IllegalStateException("No main detection model is registered");
        }
        XRayImage prepared = preprocessor.normalize(preprocessor.resize(image));

        detector.setModel(mainModel);
        AnalysisResult result = detector.analyze(prepared);
        for (Detection detection : result.getDetections()) {
            detection.setHeatmap(heatmapGenerator.generate(prepared, detection));
        }

        historyRepository.save(user, image);
        return result;
    }

    @Override
    public List<AnalysisResult> compareModels(Doctor doctor, XRayImageSource source, BodyPart bodyPart) {
        if (!doctor.isVerified()) {
            throw new IllegalStateException("Only doctors verified by an administrator can compare models");
        }
        XRayImage image = source.toXRayImage(bodyPart);
        if (!image.validate()) {
            throw new IllegalArgumentException("The X-ray image is not valid: " + image.getPath());
        }
        XRayImage prepared = preprocessor.normalize(preprocessor.resize(image));

        List<AnalysisResult> results = new ArrayList<>();
        for (DetectionModel model : modelRegistry.getAllModels()) {
            detector.setModel(model);
            AnalysisResult result = detector.analyze(prepared);
            for (Detection detection : result.getDetections()) {
                detection.setHeatmap(heatmapGenerator.generate(prepared, detection));
            }
            results.add(result);
        }

        historyRepository.save(doctor, image);
        return results;
    }

    @Override
    public List<XRayImage> getHistory(User user) {
        return historyRepository.findByUser(user);
    }
}
