# Detection model (YOLOv8, GRAZPEDWRI-DX)

The fracture model is the YOLOv8s published by Ju & Cai with their paper
("Fracture detection in pediatric wrist trauma X-ray images using YOLOv8 algorithm",
Scientific Reports 13, 20077, 2023, code and weights:
https://github.com/RuiyangJu/Bone_Fracture_Detection_YOLOv8, MIT licence).
It was trained on GRAZPEDWRI-DX (20,327 wrist X-rays of children, 9 classes) at 640 px with SGD,
so it was not retrained here.

| File | What it does |
|---|---|
| `export_onnx.py` | downloads `best.pt` (checks its SHA-256) and exports `models/yolov8-fracture.onnx` |
| `evaluate_map.ipynb` | Colab notebook: mAP on the authors' validation and test splits, compared with the paper |
| `requirements.txt` | Python packages for both |

```
pip install -r ml/requirements.txt
python ml/export_onnx.py
```

The ONNX file (43 MB) is not in git. The backend loads it from `models/yolov8-fracture.onnx`
(`YOLO_MODEL` changes the path); without it the placeholder `YoloModel` is used and the app still starts.

## GitHub Release
The file is published as an asset of the release `model-v1`:

    https://github.com/teq31/mobile-bone-fracture-detection/releases/download/model-v1/yolov8-fracture.onnx

    SHA-256  ba1b587fb3e47e79884915c11079610d7f95c230428a6f77c9038dad69180df7
    size     44,759,050 bytes

To download it for local work:

    curl -L -o models/yolov8-fracture.onnx https://github.com/teq31/mobile-bone-fracture-detection/releases/download/model-v1/yolov8-fracture.onnx

Running `export_onnx.py` again gives a working model, but with other library versions the file
can differ byte by byte, so the release keeps the exact file the checksum belongs to.

## Docker
The `Dockerfile` downloads the release asset in its `model` stage, checks the SHA-256 (the build stops
if it does not match) and puts it in the image at `/opt/model/yolov8-fracture.onnx` (`YOLO_MODEL`).
`--build-arg MODEL_URL=` builds an image without the model.
