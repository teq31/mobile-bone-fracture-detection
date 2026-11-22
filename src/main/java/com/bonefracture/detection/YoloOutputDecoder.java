package com.bonefracture.detection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Turns the raw YOLOv8 output into boxes: output[row][anchor], rows = cx, cy, w, h, then one score per class.
// Keeps the best class of each anchor, drops low scores and removes overlapping boxes of the same class (NMS).
final class YoloOutputDecoder {

    private static final int MAX_DETECTIONS = 300;

    private final float minConfidence;
    private final float iouThreshold;

    YoloOutputDecoder(float minConfidence, float iouThreshold) {
        this.minConfidence = minConfidence;
        this.iouThreshold = iouThreshold;
    }

    List<YoloBox> decode(float[][] output) {
        int classes = output.length - 4;
        int anchors = output[0].length;
        List<YoloBox> candidates = new ArrayList<>();
        for (int i = 0; i < anchors; i++) {
            int best = 0;
            for (int c = 1; c < classes; c++) {
                if (output[4 + c][i] > output[4 + best][i]) {
                    best = c;
                }
            }
            float score = output[4 + best][i];
            if (score < minConfidence) {
                continue;
            }
            float cx = output[0][i], cy = output[1][i], w = output[2][i], h = output[3][i];
            candidates.add(new YoloBox(best, score, cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2));
        }
        candidates.sort(Comparator.comparingDouble(YoloBox::confidence).reversed());

        List<YoloBox> kept = new ArrayList<>();
        for (YoloBox box : candidates) {
            if (kept.size() == MAX_DETECTIONS) {
                break;
            }
            boolean overlaps = kept.stream()
                    .anyMatch(k -> k.classId() == box.classId() && iou(k, box) > iouThreshold);
            if (!overlaps) {
                kept.add(box);
            }
        }
        return kept;
    }

    static float iou(YoloBox a, YoloBox b) {
        float w = Math.min(a.x2(), b.x2()) - Math.max(a.x1(), b.x1());
        float h = Math.min(a.y2(), b.y2()) - Math.max(a.y1(), b.y1());
        if (w <= 0 || h <= 0) {
            return 0;
        }
        float intersection = w * h;
        float union = area(a) + area(b) - intersection;
        return intersection / union;
    }

    private static float area(YoloBox box) {
        return (box.x2() - box.x1()) * (box.y2() - box.y1());
    }
}
