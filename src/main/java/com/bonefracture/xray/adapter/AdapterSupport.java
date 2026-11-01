package com.bonefracture.xray.adapter;

import com.bonefracture.xray.BodyPart;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

// Shared helper for the adapters (TDD iteration 1, step 5 - refactor).
// The checks and the PNG saving were repeated in several adapters.
final class AdapterSupport {

    private AdapterSupport() {
    }

    static void requireBodyPart(BodyPart bodyPart) {
        if (bodyPart == null) {
            throw new IllegalArgumentException("The body part of the X-ray is required");
        }
    }

    // "scan.pdf" + "-page1.png" -> "scan-page1.png", in the same folder as the source file
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
