package com.bonefracture.detection;

// One box predicted by YOLO, in the coordinates of the 640x640 model input.
// A plain class, not a record: the AspectJ compiler cannot resolve record accessors.
final class YoloBox {

    private final int classId;
    private final float confidence;
    private final float x1, y1, x2, y2;

    YoloBox(int classId, float confidence, float x1, float y1, float x2, float y2) {
        this.classId = classId;
        this.confidence = confidence;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    int classId() { return classId; }
    float confidence() { return confidence; }
    float x1() { return x1; }
    float y1() { return y1; }
    float x2() { return x2; }
    float y2() { return y2; }
}
