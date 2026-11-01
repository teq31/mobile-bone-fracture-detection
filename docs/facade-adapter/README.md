# Adapter + Facade: X-ray input and analysis

Part of Problem 1 (Mobile Bone Fracture Detection), Java.
- Lab 4: the class structure of the two patterns (declarations only).
- Lab 5: the behaviour was implemented with Test Driven Development, in two iterations,
  together with the SOA service `XRayAnalysisService`.
  The TDD steps, tests and screenshots are in [tdd/README.md](tdd/README.md).

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
- `ImagePreprocessor` and `HeatmapGenerator` are still declarations only: the real resizing and the
  EigenCAM heatmaps come with the AI model. The tests use test doubles for them.
- `HistoryRepository` keeps the history in memory for now (later SQLite on the phone, PostgreSQL on the server).
- `Student`, `Doctor`, `Patient`, `Administrator` are the team classes from the `user` package (Factory Method).
