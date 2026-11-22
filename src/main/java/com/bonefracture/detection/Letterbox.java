package com.bonefracture.detection;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

final class Letterbox {

    static Letterbox of(BufferedImage image, int size) {
        return new Letterbox();
    }

    float[] tensor() {
        return new float[0];
    }

    float scale() {
        return 0;
    }

    int padLeft() {
        return 0;
    }

    int padTop() {
        return 0;
    }

    Rectangle toImageBox(YoloBox box) {
        return null;
    }
}
