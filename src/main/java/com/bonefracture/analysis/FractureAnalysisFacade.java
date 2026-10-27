package com.bonefracture.analysis;

import com.bonefracture.detection.AnalysisResult;
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
public class FractureAnalysisFacade {

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

    // Student and Patient: one analysis with the main model
    public AnalysisResult analyzeXRay(User user, XRayImageSource source, BodyPart bodyPart) {
        // TODO source.toXRayImage -> validate -> preprocess -> detect with the main model
        //  -> heatmap for each detection -> save to history
        return null;
    }

    // Doctor: the same X-ray analyzed by every available model
    public List<AnalysisResult> compareModels(Doctor doctor, XRayImageSource source, BodyPart bodyPart) {
        // TODO
        return new ArrayList<>();
    }

    public List<XRayImage> getHistory(User user) {
        // TODO
        return new ArrayList<>();
    }
}
