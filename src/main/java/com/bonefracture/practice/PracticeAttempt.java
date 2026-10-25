package com.bonefracture.practice;

import com.bonefracture.xray.XRayImage;

import java.awt.Rectangle;
import java.time.LocalDateTime;

public class PracticeAttempt {

    private XRayImage xray;
    private Rectangle studentMark;
    private float overlap;
    private LocalDateTime date;

    public PracticeAttempt(XRayImage xray, Rectangle studentMark) {
        this.xray = xray;
        this.studentMark = studentMark;
        this.date = LocalDateTime.now();
    }

    public float getOverlap() { return overlap; }

    public XRayImage getXray() {
        return xray;
    }
}
