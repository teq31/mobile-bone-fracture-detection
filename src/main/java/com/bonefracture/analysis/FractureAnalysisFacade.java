package com.bonefracture.analysis;

import com.bonefracture.cache.Cached;
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

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Facade - single entry point for an analysis (also the XRayAnalysisService impl)
public class FractureAnalysisFacade implements XRayAnalysisService {

    private ImagePreprocessor preprocessor;
    private ModelRegistry modelRegistry;
    private FractureDetector detector;
    private HeatmapGenerator heatmapGenerator;
    private HistoryRepository historyRepository;
    private boolean contrastStretching = true;

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

    public FractureAnalysisFacade(ImagePreprocessor preprocessor,
                                  ModelRegistry modelRegistry,
                                  FractureDetector detector,
                                  HeatmapGenerator heatmapGenerator,
                                  HistoryRepository historyRepository,
                                  boolean contrastStretching) {
        this(preprocessor, modelRegistry, detector, heatmapGenerator, historyRepository);
        this.contrastStretching = contrastStretching;
    }

    @Override
    public AnalysisResult analyzeXRay(User user, XRayImageSource source, BodyPart bodyPart) {
        XRayImage image = convertAndValidate(source, bodyPart);
        DetectionModel mainModel = modelRegistry.getMainModel(); // throws IllegalStateException if there is none

        XRayImage prepared = preprocess(image);
        AnalysisResult result = onOriginal(runModel(mainModel, prepared), prepared, image);
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
        for (AnalysisResult result : compareModels(prepared)) {
            addHeatmaps(result, prepared);
            AnalysisResult onOriginal = onOriginal(result, prepared, image);
            keepOnOriginal(image, onOriginal);
            results.add(onOriginal);
        }

        historyRepository.save(doctor, image);
        return results;
    }

    // cached by image hash for 10 min (Federica's @Cached)
    @Cached(name = "model-comparison", ttlSeconds = 600, maxEntries = 100)
    public List<AnalysisResult> compareModels(XRayImage image) {
        List<AnalysisResult> results = new ArrayList<>();
        for (DetectionModel model : modelRegistry.getAllModels()) {
            results.add(model.detect(image));
        }
        return List.copyOf(results);
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
        XRayImage resized = preprocessor.resize(image);
        return contrastStretching ? preprocessor.normalize(resized) : resized;
    }

    // detector saves the result on the resized copy, history needs it on the original
    private void keepOnOriginal(XRayImage original, AnalysisResult result) {
        if (!original.getResults().contains(result)) {
            original.addResult(result);
        }
    }

    // The model saw the resized copy, the app draws the boxes on the uploaded X-ray.
    // A new result is returned: the one from the model may come from the cache and must stay as it is.
    private AnalysisResult onOriginal(AnalysisResult result, XRayImage prepared, XRayImage original) {
        Dimension from = sizeOf(prepared);
        Dimension to = sizeOf(original);
        if (from == null || to == null || from.equals(to)) {
            return result;
        }
        double scaleX = (double) to.width / from.width;
        double scaleY = (double) to.height / from.height;
        AnalysisResult mapped = new AnalysisResult(result.getModel());
        for (Detection detection : result.getDetections()) {
            Rectangle box = detection.getBoundingBox();
            if (box != null) {
                int x1 = (int) Math.round(box.x * scaleX);
                int y1 = (int) Math.round(box.y * scaleY);
                int x2 = (int) Math.round((box.x + box.width) * scaleX);
                int y2 = (int) Math.round((box.y + box.height) * scaleY);
                box = new Rectangle(x1, y1, x2 - x1, y2 - y1);
            }
            mapped.addDetection(new Detection(detection.getLabel(), detection.getConfidence(), box,
                    detection.getHeatmap()));
        }
        return mapped;
    }

    // width and height without decoding the whole image; null if it cannot be read
    private static Dimension sizeOf(XRayImage image) {
        try (ImageInputStream in = ImageIO.createImageInputStream(new File(image.getPath()))) {
            Iterator<ImageReader> readers = in == null ? null : ImageIO.getImageReaders(in);
            if (readers == null || !readers.hasNext()) {
                return null;
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(in);
                return new Dimension(reader.getWidth(0), reader.getHeight(0));
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            return null;
        }
    }

    private AnalysisResult runModel(DetectionModel model, XRayImage prepared) {
        detector.setModel(model);
        AnalysisResult result = detector.analyze(prepared);
        addHeatmaps(result, prepared);
        return result;
    }

    private void addHeatmaps(AnalysisResult result, XRayImage prepared) {
        for (Detection detection : result.getDetections()) {
            detection.setHeatmap(heatmapGenerator.generate(prepared, detection));
        }
    }
}
