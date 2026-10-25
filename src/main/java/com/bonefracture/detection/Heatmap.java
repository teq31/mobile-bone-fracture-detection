package com.bonefracture.detection;

import java.awt.Image;

public class Heatmap {

    private Image image;

    public Heatmap(Image image) {
        this.image = image;
    }

    public Image getImage() { return image; }
}
