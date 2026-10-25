package com.bonefracture.detection;

import com.bonefracture.xray.XRayImage;

// Facade pattern - subsystem class
// Builds the heatmap that explains a detection (visual justification).
public class HeatmapGenerator {

    private String method = "EigenCAM";

    public Heatmap generate(XRayImage image, Detection detection) {
        // TODO
        return null;
    }

    public String getMethod() { return method; }
}
