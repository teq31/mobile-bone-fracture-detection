package com.bonefracture.detection;

import java.util.ArrayList;
import java.util.List;

// Facade pattern - subsystem class
// Keeps the available detection models (the main one for students and patients,
// all of them for doctors).
public class ModelRegistry {

    private List<DetectionModel> models = new ArrayList<>();

    public void register(DetectionModel model) {
        models.add(model);
    }

    public DetectionModel getMainModel() {
        for (DetectionModel model : models) {
            if (model.isMain()) {
                return model;
            }
        }
        return null;
    }

    public List<DetectionModel> getAllModels() { return models; }
}
