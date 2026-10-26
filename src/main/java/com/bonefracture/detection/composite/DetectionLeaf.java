package com.bonefracture.detection.composite;

import com.bonefracture.detection.Detection;
import com.bonefracture.detection.DetectionModel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DetectionLeaf implements DetectionComponent{

    private Detection detection;
    private DetectionModel model;

    public DetectionLeaf(Detection detection, DetectionModel model)
    {
        this.detection=detection;
        this.model=model;
    }
    @Override
    public Rectangle getBoundingBox()
    {
        return detection.getBoundingBox();
    }

    @Override
    public float getConfidence()
    {
        return detection.getConfidence();
    }

    @Override
    public String getLabel(){
        return detection.getLabel();
    }

    @Override
    public List<DetectionModel> getModels(){
        List<DetectionModel> models=new ArrayList<>();
        models.add(model);
        return models;
    }

    @Override
    public int getAgreementCount() {
        return 1;
    }

    @Override
    public void add(DetectionComponent component) {

    }

    @Override
    public void remove(DetectionComponent component) {

    }

    @Override
    public List<DetectionComponent> getChildren() {
        return List.of();
    }

    public Detection getDetection()
    {
        return detection;
    }
}
