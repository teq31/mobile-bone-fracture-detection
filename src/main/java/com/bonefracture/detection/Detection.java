package com.bonefracture.detection;

import java.awt.Rectangle;

public class Detection {
    private String label;
    private float confidence;
    private Rectangle boundingBox;
    private Heatmap heatmap;

    public Detection(String label, float confidence, Rectangle boundingBox, Heatmap heatmap) {
        this.label = label;
        this.confidence = confidence;
        this.boundingBox = boundingBox;
        this.heatmap = heatmap;
    }

    public String getLabel() { return label; }

    public float getConfidence() { return confidence; }

    public Rectangle getBoundingBox() { return boundingBox; }

    public Heatmap getHeatmap() {
        return heatmap;
    }
}
