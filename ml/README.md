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
With docker-compose the `models` folder is mounted into the backend container.
