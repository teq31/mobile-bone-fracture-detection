package com.bonefracture.xray.adapter;

import com.bonefracture.xray.BodyPart;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

// stuff the adapters had in common (refactor, tdd it. 1)
final class AdapterSupport {

    private AdapterSupport() {
    }

    static void requireBodyPart(BodyPart bodyPart) {
        if (bodyPart == null) {
            throw new IllegalArgumentException("The body part of the X-ray is required");
        }
    }

    // scan.pdf -> scan-page1.png
    static File siblingFile(File source, String extensionToDrop, String newSuffix) {
        String baseName = source.getName().replaceFirst("(?i)\\." + extensionToDrop + "$", "");
        return new File(source.getParentFile(), baseName + newSuffix);
    }

    static void savePng(BufferedImage image, File png) {
        try {
            ImageIO.write(image, "png", png);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save the image as PNG: " + png.getPath(), e);
        }
    }
}
