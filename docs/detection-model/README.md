# Detection model: YOLOv8 with ONNX Runtime

Part of Problem 1, labs 6-7. The `DetectionModel` Strategy from lab 4 now has a real model.

## Model
YOLOv8s trained by Ju & Cai on GRAZPEDWRI-DX (20,327 wrist X-rays of children, 9 classes),
640 px, SGD. The authors publish the weights with their paper, so the model was not retrained:
`ml/export_onnx.py` downloads them and exports `models/yolov8-fracture.onnx` (see `ml/README.md`).

## Results
Evaluated with `ml/evaluate_map.ipynb` on the authors' own split (`csv/valid_data.csv`,
`csv/test_data.csv` in their repository), 640 px, `conf=0.001`, `iou=0.6`, as in the paper.
The validation set has 3,740 fracture boxes, the same number as Table 3 of the paper.

| | mAP50 | mAP50-95 | fracture AP50 | fracture AP50-95 |
|---|---|---|---|---|
| Paper, YOLOv8s 640 SGD, validation (Table 2) | 0.611 | 0.389 | - | - |
| Paper, YOLOv8s 1024, validation (Table 3) | 0.623 | 0.395 | 0.947 | 0.572 |
| Ours, validation (4,094 images) | **0.630** | **0.388** | **0.940** | **0.547** |
| Ours, test (2,029 images) | **0.588** | **0.383** | **0.922** | **0.522** |

- On validation the model matches the paper: mAP50-95 0.388 vs 0.389, mAP50 a bit higher (0.630 vs 0.611).
- The paper reports no test-set numbers; on test mAP50 is lower (0.588), mostly because of the rare
  classes (bone lesion AP50 0.13, bone anomaly 0.01). Foreign body has no boxes in either split.
- Fractures, the class the app is about, are found well: AP50 0.94 on validation and 0.92 on test.
- Speed on 4 CPU cores: about 75 ms of inference per image in Python, about 160 ms for a whole
  analysis in Java (reading the 16-bit PNG, letterbox, inference and NMS, measured on 30 test X-rays).

## Java: YoloOnnxModel
`detection.YoloOnnxModel extends DetectionModel` (Strategy) runs the ONNX file with ONNX Runtime:

1. `Letterbox`: resizes the X-ray to 640x640 keeping the aspect ratio, grey padding (114), RGB values 0..1.
   16-bit grey PNGs keep their 8 high bits, like OpenCV, so the input is the same as in Python.
2. ONNX Runtime returns a `[1, 13, 8400]` tensor: box (cx, cy, w, h) + 9 class scores for 8,400 anchors.
3. `YoloOutputDecoder`: best class per anchor, confidence >= 0.25, NMS per class (IoU 0.45), max 300 boxes.
4. Boxes are mapped back to the original image and returned as `Detection`s in an `AnalysisResult`.
   Class names are read from the model's metadata.

The model file is loaded on the first analysis. `AnalysisBeansConfig` registers `YoloOnnxModel` as
the main model when `models/yolov8-fracture.onnx` exists (path: `YOLO_MODEL`), otherwise the
placeholder `YoloModel`, so the app starts without the file.

Two aspects of the team already apply to `YoloOnnxModel` through `DetectionModel+.detect(..)`:
- `CachingAspect` (this branch): the result for the same X-ray comes from the cache, so it is not
  analysed twice;
- `PerformanceMonitoringAspect` (Teodora, branch `teodora/facade-adapter`): times every analysis step,
  including the inference.

## Tests
- `LetterboxTest`: scale and padding, grey padding, RGB 0..1, 16-bit grey, boxes mapped back and clipped
- `YoloOutputDecoderTest`: threshold, centre to corners, best class, NMS, different classes kept, order
- `YoloOnnxModelTest`: class names from the metadata, missing file, and the same 5 boxes as
  Ultralytics on a GRAZPEDWRI-DX test image (confidence within 0.01, boxes within 3 px).
  The last one is skipped when the model file is missing.
- `AnalysisBeansConfigTest`: ONNX model as main model when the file exists, placeholder otherwise
