package com.bonefracture.detection;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class YoloOutputDecoderTest {

    private static final int CLASSES = 2;

    private final YoloOutputDecoder decoder = new YoloOutputDecoder(0.25f, 0.45f);

    // rows: cx, cy, w, h, score class 0, score class 1; one column per anchor
    private static float[][] output(float[]... anchors) {
        float[][] out = new float[4 + CLASSES][anchors.length];
        for (int i = 0; i < anchors.length; i++) {
            for (int row = 0; row < 4 + CLASSES; row++) {
                out[row][i] = anchors[i][row];
            }
        }
        return out;
    }

    @Test
    void lowScoresAreIgnored() {
        List<YoloBox> boxes = decoder.decode(output(new float[]{100, 100, 40, 20, 0.1f, 0.2f}));
        assertTrue(boxes.isEmpty());
    }

    @Test
    void centreFormatIsConvertedToCorners() {
        YoloBox box = decoder.decode(output(new float[]{100, 100, 40, 20, 0.9f, 0f})).get(0);
        assertEquals(80, box.x1(), 1e-4);
        assertEquals(90, box.y1(), 1e-4);
        assertEquals(120, box.x2(), 1e-4);
        assertEquals(110, box.y2(), 1e-4);
    }

    @Test
    void bestClassWins() {
        YoloBox box = decoder.decode(output(new float[]{100, 100, 40, 20, 0.3f, 0.8f})).get(0);
        assertEquals(1, box.classId());
        assertEquals(0.8f, box.confidence(), 1e-6);
    }

    @Test
    void overlappingBoxesOfTheSameClassKeepOnlyTheBest() {
        List<YoloBox> boxes = decoder.decode(output(
                new float[]{100, 100, 40, 40, 0.6f, 0f},
                new float[]{102, 101, 40, 40, 0.9f, 0f},
                new float[]{300, 300, 40, 40, 0.5f, 0f}));
        assertEquals(2, boxes.size());
        assertEquals(0.9f, boxes.get(0).confidence(), 1e-6);
        assertEquals(0.5f, boxes.get(1).confidence(), 1e-6);
    }

    @Test
    void overlappingBoxesOfDifferentClassesAreBothKept() {
        List<YoloBox> boxes = decoder.decode(output(
                new float[]{100, 100, 40, 40, 0.9f, 0f},
                new float[]{100, 100, 40, 40, 0f, 0.7f}));
        assertEquals(2, boxes.size());
    }

    @Test
    void resultIsSortedByConfidence() {
        List<YoloBox> boxes = decoder.decode(output(
                new float[]{100, 100, 20, 20, 0.4f, 0f},
                new float[]{300, 300, 20, 20, 0.95f, 0f},
                new float[]{500, 500, 20, 20, 0f, 0.7f}));
        assertEquals(List.of(0.95f, 0.7f, 0.4f), boxes.stream().map(YoloBox::confidence).toList());
    }
}
