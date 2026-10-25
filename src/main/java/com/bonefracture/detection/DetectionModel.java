package com.bonefracture.detection;

import com.bonefracture.xray.XRayImage;

public abstract class DetectionModel {

    protected String name;
    protected String version;
    protected boolean isMain;

    public DetectionModel(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public abstract AnalysisResult detect(XRayImage image);

    public String getName() { return name; }

    public String getVersion() {
        return version;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) { isMain = main; }
}
