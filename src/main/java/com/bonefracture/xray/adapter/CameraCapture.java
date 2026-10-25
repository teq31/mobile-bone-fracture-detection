package com.bonefracture.xray.adapter;

import java.time.LocalDateTime;

// Adapter pattern - Adaptee
// Stands for the phone camera plugin, which returns a raw photo (bytes + size),
// not an XRayImage.
public class CameraCapture {

    private byte[] jpegBytes;
    private int width;
    private int height;
    private LocalDateTime capturedAt;

    public CameraCapture(byte[] jpegBytes, int width, int height) {
        this.jpegBytes = jpegBytes;
        this.width = width;
        this.height = height;
        this.capturedAt = LocalDateTime.now();
    }

    public byte[] getJpegBytes() { return jpegBytes; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public LocalDateTime getCapturedAt() { return capturedAt; }
}
