package com.bonefracture.detection.composite;

import com.bonefracture.detection.DetectionModel;

import java.awt.*;
import java.util.List;

public interface DetectionComponent {

    Rectangle getBoundingBox();

    float getConfidence();

    String getLabel();

    List<DetectionModel> getModels();

    int getAgreementCount();

    void add(DetectionComponent component);

    void remove(DetectionComponent component);

    List<DetectionComponent> getChildren();
}
