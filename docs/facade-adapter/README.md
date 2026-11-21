# Adapter + Facade: X-ray input and analysis

Part of Problem 1 (Mobile Bone Fracture Detection), Java.
- Lab 4: the class structure of the two patterns (declarations only).
- Lab 5: the behaviour was implemented with Test Driven Development, in two iterations,
  together with the SOA service `XRayAnalysisService`.
  The TDD steps, tests and screenshots are in [tdd/README.md](tdd/README.md).
- Labs 6-7: the analysis pipeline works on real images (preprocessing, heatmaps) and is timed by an
  aspect (AOP), see [Labs 6-7](#labs-6-7-a-working-pipeline-and-aop) below.

Run the tests with `mvn test`.

## Adapter: every input becomes an XRayImage
| Role in the pattern | Class |
|---|---|
| Target | `xray.adapter.XRayImageSource` (`toXRayImage(bodyPart)`) |
| Adapters | `ImageFileAdapter` (JPG / PNG), `PdfXRayAdapter`, `CameraXRayAdapter`, `DicomXRayAdapter` |
| Adaptees (stand for external libraries) | `PdfDocument` (e.g. Apache PDFBox), `CameraCapture` (phone camera), `DicomFile` (e.g. dcm4che) |
| Client | `analysis.FractureAnalysisFacade` |

Object Adapter: each adapter contains its Adaptee (composition) and converts it to `XRayImage`.
A PDF page and a DICOM image are saved as PNG next to the source file; a camera photo is saved as JPG.
`DicomXRayAdapter` takes the body part from the DICOM metadata when the user does not choose one
and never copies patient identifiers. The shared code is in `AdapterSupport`.

## Facade: one entry point for an analysis
| Role in the pattern | Class |
|---|---|
| Facade | `analysis.FractureAnalysisFacade` |
| Subsystems | `XRayImageSource` (Adapter), `ImagePreprocessor`, `ModelRegistry`, `FractureDetector` (Strategy), `HeatmapGenerator`, `HistoryRepository` |
| Clients | the app screens (later the REST API) |

`analyzeXRay()` runs the steps in order: convert the source -> validate -> preprocess -> detect with the
main model -> heatmap for every detection -> save to history. `compareModels()` does the same with every
registered model, only for a doctor verified by an administrator.

## Service (SOA)
| Service | Implementation | What it does |
|---|---|---|
| `analysis.XRayAnalysisService` | `FractureAnalysisFacade` | `analyzeXRay` (Student / Patient, main model), `compareModels` (verified Doctor, every model), `getHistory` |

## Notes
- Sources are in `src/main/java/com/bonefracture/` (`xray.adapter`, `analysis`, `detection`, `history`),
  tests in `src/test/java/com/bonefracture/`.
- The facade tests (`XRayAnalysisServiceTest`) still use test doubles for the preprocessor, the models and the
  heatmaps, so they do not depend on image files or on the AI model.
- `HistoryRepository` keeps the history in memory for now (later SQLite on the phone, PostgreSQL on the server).
- `Student`, `Doctor`, `Patient`, `Administrator` are the team classes from the `user` package (Factory Method).

## Labs 6-7: a working pipeline and AOP

### Real preprocessing and heatmaps
| Class | What it does now |
|---|---|
| `xray.ImagePreprocessor` | `resize`: grey scale, longest side 640 px (input size of the YOLO model), aspect ratio kept. `normalize`: contrast stretching between the 1st and 99th percentile, so a few burned pixels or labels do not decide the contrast. Each step saves a new PNG (`wrist-resized.png`, `wrist-resized-normalized.png`); the uploaded X-ray is never changed. |
| `detection.HeatmapGenerator` | a transparent image the size of the X-ray, red in the centre of the detection and fading through green and blue (jet colour map), stronger when the confidence is higher. It is box-based because the model does not expose its activations yet; EigenCAM can replace it later without changing the facade. |
| `analysis.FractureAnalysisFacade` | the model runs on the preprocessed copy, but the result is also kept on the uploaded X-ray, so the history shows it. |

The bounding boxes and the heatmap are in the coordinates of the preprocessed image, so the app shows them over
that image (`imageUrl` in the API).

### AOP: PerformanceMonitoringAspect
Timing is a cross-cutting concern: every step of an analysis should be measured, but none of the classes should
contain timing code. The aspect is woven at compile time by AspectJ (same setup as the team's other aspects).

| Pointcut (method execution) | Example of a recorded operation |
|---|---|
| `public * XRayAnalysisService+.*(..)` | `FractureAnalysisFacade.analyzeXRay`, `FractureAnalysisFacade.compareModels`, `FractureAnalysisFacade.getHistory` |
| `XRayImage XRayImageSource+.toXRayImage(..)` | `ImageFileAdapter.toXRayImage`, `PdfXRayAdapter.toXRayImage`, ... |
| `XRayImage ImagePreprocessor+.*(..)` | `ImagePreprocessor.resize`, `ImagePreprocessor.normalize` |
| `AnalysisResult DetectionModel+.detect(..)` | `YoloModel.detect`, `RfDetrModel.detect` |
| `Heatmap HeatmapGenerator+.generate(..)` | `HeatmapGenerator.generate` |

- One `@Around` advice measures the call with `System.nanoTime()` and records it in
  `performance.PerformanceMonitor`: calls, failures, average / min / max / last duration in ms, per
  `ClassName.method` (the class that did the work, not the interface).
- A call slower than 2 s (`PerformanceMonitor.setSlowThresholdMillis`) is logged as a warning.
- An exception is counted as a failure and thrown again unchanged, so the aspect never changes the behaviour.
- `PerformanceMonitor.all()` lists the operations, slowest first. After the merge with the Spring Boot backend it
  can be exposed as `GET /api/performance/stats`, like `GET /api/cache/stats`.
- `DetectionModel+` and `HeatmapGenerator+` include every subclass, so a new model is timed without any change.

### Tests
| Test class | What it checks |
|---|---|
| `ImagePreprocessorTest` (6) | longest side 640 px with the aspect ratio kept (also for small images); colour -> grey; contrast stretched to 0-255; a flat image stays the same; an unreadable file is rejected |
| `HeatmapGeneratorTest` (3) | same size as the X-ray; strongest in the centre of the detection, nothing far away; lower confidence gives a weaker heatmap |
| `XRayAnalysisServiceTest` (+1) | the result stays on the uploaded X-ray when preprocessing creates a new image |
| `PerformanceMonitorTest` (5) | calls, failures, average / min / max / last; slowest first; slow-call threshold; reset |
| `PerformanceMonitoringAspectTest` (4) | a real analysis: the facade, the adapter, both preprocessing steps and the model are timed; the whole analysis lasts at least as long as its steps; a failed call is counted and the exception still reaches the caller; `getHistory` is timed |

`PerformanceMonitoringAspectTest` only passes when the aspect is woven, that is with `mvn test`
(the build log shows "Join point ... advised by around advice from PerformanceMonitoringAspect").
