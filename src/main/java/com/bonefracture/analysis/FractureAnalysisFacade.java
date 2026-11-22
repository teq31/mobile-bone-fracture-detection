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

// Facade - single entry point for an analysis (also the XRayAnalysisService impl)
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
        XRayImage image = convertAndValidate(source, bodyPart);
        DetectionModel mainModel = modelRegistry.getMainModel(); // throws IllegalStateException if there is none

        AnalysisResult result = runModel(mainModel, preprocess(image));
        keepOnOriginal(image, result);

        historyRepository.save(user, image);
        return result;
    }

    @Override
    public List<AnalysisResult> compareModels(Doctor doctor, XRayImageSource source, BodyPart bodyPart) {
        if (!doctor.isVerified()) {
            throw new IllegalStateException("Only doctors verified by an administrator can compare models");
        }
        XRayImage image = convertAndValidate(source, bodyPart);
        XRayImage prepared = preprocess(image);

        List<AnalysisResult> results = new ArrayList<>();
        for (DetectionModel model : modelRegistry.getAllModels()) {
            AnalysisResult result = runModel(model, prepared);
            keepOnOriginal(image, result);
            results.add(result);
        }

        historyRepository.save(doctor, image);
        return results;
    }

    @Override
    public List<XRayImage> getHistory(User user) {
        return historyRepository.findByUser(user);
    }

    // shared steps (refactor, tdd it. 2)

    private XRayImage convertAndValidate(XRayImageSource source, BodyPart bodyPart) {
        XRayImage image = source.toXRayImage(bodyPart);
        if (!image.validate()) {
            throw new IllegalArgumentException("The X-ray image is not valid: " + image.getPath());
        }
        return image;
    }

    private XRayImage preprocess(XRayImage image) {
        return preprocessor.normalize(preprocessor.resize(image));
    }

    // detector saves the result on the resized copy, history needs it on the original
    private void keepOnOriginal(XRayImage original, AnalysisResult result) {
        if (!original.getResults().contains(result)) {
            original.addResult(result);
        }
    }

    private AnalysisResult runModel(DetectionModel model, XRayImage prepared) {
        detector.setModel(model);
        AnalysisResult result = detector.analyze(prepared);
        for (Detection detection : result.getDetections()) {
            detection.setHeatmap(heatmapGenerator.generate(prepared, detection));
        }
        return result;
    }
}
