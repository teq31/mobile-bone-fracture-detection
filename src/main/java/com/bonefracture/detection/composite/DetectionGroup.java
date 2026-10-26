package com.bonefracture.detection.composite;

import com.bonefracture.detection.DetectionModel;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;


public class DetectionGroup implements DetectionComponent {

    public static final float DEFAULT_MIN_IOU = 0.5f;

    private List<DetectionComponent> children = new ArrayList<>();
    private float minIou;

    public DetectionGroup() {
        this.minIou = DEFAULT_MIN_IOU;
    }

    public DetectionGroup(float minIou) {
        this.minIou = minIou;
    }

    @Override
    public Rectangle getBoundingBox() {
        // TODO union or average of the children boxes
        return null;
    }

    @Override
    public float getConfidence() {
        // TODO average confidence of the children
        return 0;
    }

    @Override
    public String getLabel() {
        // TODO most frequent label among children
        return null;
    }

    @Override
    public List<DetectionModel> getModels() {
        // TODO distinct models over all children
        return null;
    }

    @Override
    public int getAgreementCount() {
        // TODO number of distinct models
        return 0;
    }

    @Override
    public void add(DetectionComponent component) { children.add(component); }

    @Override
    public void remove(DetectionComponent component) { children.remove(component); }

    @Override
    public List<DetectionComponent> getChildren() { return children; }

    public boolean accepts(DetectionComponent component) {
        // TODO
        return false;
    }

    public AgreementLevel getAgreementLevel(int totalModels) {
        // TODO NONE / PARTIAL / FULL
        return null;
    }

    private float intersectionOverUnion(Rectangle a, Rectangle b) {
        // TODO
        return 0;
    }

    public float getMinIou() { return minIou; }
}