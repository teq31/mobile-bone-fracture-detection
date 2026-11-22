"""Downloads the YOLOv8 weights trained on GRAZPEDWRI-DX by Ju & Cai and exports them to ONNX.

The result is models/yolov8-fracture.onnx, the file YoloOnnxModel loads in the backend.

    pip install -r ml/requirements.txt
    python ml/export_onnx.py
"""
import hashlib
import shutil
import urllib.request
from pathlib import Path

from ultralytics import YOLO

WEIGHTS_URL = "https://github.com/RuiyangJu/Bone_Fracture_Detection_YOLOv8/releases/download/Trained_model/best.pt"
WEIGHTS_SHA256 = "301abc1774c28dd7c5adbcf1e8a79ed6771273615238b62dd179b622292b3a81"

ROOT = Path(__file__).resolve().parent.parent
MODELS = ROOT / "models"
WEIGHTS = MODELS / "yolov8-fracture.pt"
ONNX = MODELS / "yolov8-fracture.onnx"


def download_weights():
    if not WEIGHTS.exists():
        MODELS.mkdir(exist_ok=True)
        print("downloading", WEIGHTS_URL)
        urllib.request.urlretrieve(WEIGHTS_URL, WEIGHTS)
    digest = hashlib.sha256(WEIGHTS.read_bytes()).hexdigest()
    if digest != WEIGHTS_SHA256:
        raise SystemExit(f"unexpected weights file {WEIGHTS} (sha256 {digest})")


def export():
    model = YOLO(str(WEIGHTS))
    print("classes:", model.names)
    exported = Path(model.export(format="onnx", imgsz=640, opset=17, simplify=True, dynamic=False))
    if exported.resolve() != ONNX.resolve():
        shutil.move(exported, ONNX)
    print("saved", ONNX)


if __name__ == "__main__":
    download_weights()
    export()
