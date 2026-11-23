package com.bonefracture.detection.composite;

import com.bonefracture.detection.DetectionModel;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        if (children.isEmpty()) {
            return null;
        }
        Rectangle union = null;
        for (DetectionComponent child : children) {
            Rectangle box = child.getBoundingBox();
            if (box == null) {
                continue;
            }
            union = (union == null) ? new Rectangle(box) : union.union(box);
        }
        return union;
    }

    @Override
    public float getConfidence() {
        if (children.isEmpty()) {
            return 0;
        }
        float sum = 0;
        for (DetectionComponent child : children) {
            sum += child.getConfidence();
        }
        return sum / children.size();
    }

    @Override
    public String getLabel() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (DetectionComponent child : children) {
            String label = child.getLabel();
            counts.merge(label, 1, Integer::sum);
        }
        String best = null;
        int bestCount = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                best = entry.getKey();
            }
        }
        return best;
    }

    @Override
    public List<DetectionModel> getModels() {
        List<DetectionModel> models = new ArrayList<>();
        for (DetectionComponent child : children) {
            for (DetectionModel model : child.getModels()) {
                if (!models.contains(model)) {
                    models.add(model);
                }
            }
        }
        return models;
    }

    @Override
    public int getAgreementCount() {
        return getModels().size();
    }

    @Override
    public void add(DetectionComponent component) { children.add(component); }

    @Override
    public void remove(DetectionComponent component) { children.remove(component); }

    @Override
    public List<DetectionComponent> getChildren() { return children; }

    public boolean accepts(DetectionComponent component) {
        Rectangle current = getBoundingBox();
        if (current == null) {
            return true;
        }
        return intersectionOverUnion(current, component.getBoundingBox()) >= minIou;
    }

    public AgreementLevel getAgreementLevel(int totalModels) {
        int count = getAgreementCount();
        if (count <= 1) {
            return AgreementLevel.NONE;
        }
        if (count >= totalModels) {
            return AgreementLevel.FULL;
        }
        return AgreementLevel.PARTIAL;
    }

    private float intersectionOverUnion(Rectangle a, Rectangle b) {
        if (a == null || b == null) {
            return 0;
        }
        Rectangle intersection = a.intersection(b);
        if (intersection.isEmpty()) {
            return 0;
        }
        float intersectionArea = (float) intersection.width * intersection.height;
        float unionArea = (float) a.width * a.height + (float) b.width * b.height - intersectionArea;
        return unionArea <= 0 ? 0 : intersectionArea / unionArea;
    }

    public float getMinIou() { return minIou; }
}
