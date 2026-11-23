package com.bonefracture.detection;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.bonefracture.xray.XRayImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Strategy: the real YOLOv8 model, trained on GRAZPEDWRI-DX and exported to ONNX (ml/export_onnx.py).
// The model file is loaded on the first analysis, so the app starts even if the file is missing.
public class YoloOnnxModel extends DetectionModel implements AutoCloseable {

    public static final int INPUT_SIZE = 640;

    private static final Pattern NAME = Pattern.compile("(\\d+)\\s*:\\s*'([^']*)'");

    private final Path modelPath;
    private final YoloOutputDecoder decoder;
    private OrtEnvironment environment;
    private OrtSession session;
    private List<String> classNames;

    public YoloOnnxModel(String version, String modelPath) {
        this(version, modelPath, 0.25f, 0.45f);
    }

    public YoloOnnxModel(String version, String modelPath, float minConfidence, float iouThreshold) {
        super("YOLO", version);
        this.modelPath = Path.of(modelPath);
        this.decoder = new YoloOutputDecoder(minConfidence, iouThreshold);
    }

    public boolean isAvailable() {
        return Files.isRegularFile(modelPath);
    }

    @Override
    public AnalysisResult detect(XRayImage image) {
        load();
        Letterbox letterbox = Letterbox.of(read(image), INPUT_SIZE);
        AnalysisResult result = new AnalysisResult(this);
        for (YoloBox box : decoder.decode(run(letterbox.tensor()))) {
            result.addDetection(new Detection(classNames.get(box.classId()), box.confidence(),
                    letterbox.toImageBox(box), null));
        }
        return result;
    }

    private synchronized void load() {
        if (session != null) {
            return;
        }
        if (!isAvailable()) {
            throw new IllegalStateException("YOLO model file not found: " + modelPath.toAbsolutePath()
                    + " (create it with ml/export_onnx.py)");
        }
        try {
            environment = OrtEnvironment.getEnvironment();
            session = environment.createSession(modelPath.toString(), new OrtSession.SessionOptions());
            classNames = parseNames(session.getMetadata().getCustomMetadata().get("names"));
        } catch (OrtException e) {
            throw new IllegalStateException("cannot load the YOLO model " + modelPath, e);
        }
    }

    private float[][] run(float[] tensor) {
        long[] shape = {1, 3, INPUT_SIZE, INPUT_SIZE};
        try (OnnxTensor input = OnnxTensor.createTensor(environment, FloatBuffer.wrap(tensor), shape);
             OrtSession.Result output = session.run(Map.of(session.getInputNames().iterator().next(), input))) {
            return ((float[][][]) output.get(0).getValue())[0];
        } catch (OrtException e) {
            throw new IllegalStateException("YOLO inference failed", e);
        }
    }

    private static BufferedImage read(XRayImage image) {
        try {
            BufferedImage picture = ImageIO.read(new File(image.getPath()));
            if (picture == null) {
                throw new IllegalArgumentException("not an image: " + image.getPath());
            }
            return picture;
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + image.getPath(), e);
        }
    }

    // Ultralytics stores the class names as "{0: 'boneanomaly', 1: 'bonelesion', ...}"
    static List<String> parseNames(String metadata) {
        Map<Integer, String> names = new TreeMap<>();
        Matcher m = NAME.matcher(metadata == null ? "" : metadata);
        while (m.find()) {
            names.put(Integer.parseInt(m.group(1)), m.group(2));
        }
        return new ArrayList<>(names.values());
    }

    @Override
    public synchronized void close() throws OrtException {
        if (session != null) {
            session.close();
            session = null;
        }
    }
}
