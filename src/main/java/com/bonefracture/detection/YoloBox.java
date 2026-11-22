package com.bonefracture.detection;

// One box predicted by YOLO, in the coordinates of the 640x640 model input
record YoloBox(int classId, float confidence, float x1, float y1, float x2, float y2) {
}
