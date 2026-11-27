package com.bonefracture.xray;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

// Phone cameras often store the photo sideways and put the turn in the EXIF "Orientation" tag.
// ImageIO ignores the tag, so the model would get a sideways X-ray (and the app shows it upright).
// makeUpright turns the pixels and saves the photo again without EXIF.
public final class ExifOrientation {

    private static final int ORIENTATION_TAG = 0x0112;

    private ExifOrientation() {
    }

    // 1 = upright (also when the file has no EXIF); 2..8 as in the EXIF standard
    static int read(byte[] jpeg) {
        if (jpeg.length < 4 || (jpeg[0] & 0xFF) != 0xFF || (jpeg[1] & 0xFF) != 0xD8) {
            return 1;
        }
        int pos = 2;
        while (pos + 4 <= jpeg.length && (jpeg[pos] & 0xFF) == 0xFF) {
            int marker = jpeg[pos + 1] & 0xFF;
            if (marker == 0xDA || marker == 0xD9) {
                break; // image data, no more headers
            }
            int length = u16(jpeg, pos + 2, false);
            int end = pos + 2 + length;
            if (marker == 0xE1 && length >= 16 && end <= jpeg.length && isExif(jpeg, pos + 4)) {
                return orientation(jpeg, pos + 10, end);
            }
            pos = end;
        }
        return 1;
    }

    private static boolean isExif(byte[] b, int at) {
        return b[at] == 'E' && b[at + 1] == 'x' && b[at + 2] == 'i' && b[at + 3] == 'f' && b[at + 4] == 0 && b[at + 5] == 0;
    }

    // TIFF header at tiff, then the first IFD: 2-byte count and 12-byte entries
    private static int orientation(byte[] b, int tiff, int end) {
        boolean littleEndian = b[tiff] == 'I' && b[tiff + 1] == 'I';
        int ifd = tiff + (int) u32(b, tiff + 4, littleEndian);
        if (ifd < tiff || ifd + 2 > end) {
            return 1;
        }
        int entries = u16(b, ifd, littleEndian);
        for (int i = 0; i < entries; i++) {
            int entry = ifd + 2 + i * 12;
            if (entry + 12 > end) {
                break;
            }
            if (u16(b, entry, littleEndian) == ORIENTATION_TAG) {
                int value = u16(b, entry + 8, littleEndian);
                return value >= 1 && value <= 8 ? value : 1;
            }
        }
        return 1;
    }

    private static int u16(byte[] b, int at, boolean littleEndian) {
        int first = b[at] & 0xFF, second = b[at + 1] & 0xFF;
        return littleEndian ? first | second << 8 : first << 8 | second;
    }

    private static long u32(byte[] b, int at, boolean littleEndian) {
        long high = u16(b, littleEndian ? at + 2 : at, littleEndian);
        long low = u16(b, littleEndian ? at : at + 2, littleEndian);
        return high << 16 | low;
    }

    // true if the photo was turned and saved again
    public static boolean makeUpright(File jpeg) throws IOException {
        int orientation = read(Files.readAllBytes(jpeg.toPath()));
        if (orientation == 1) {
            return false;
        }
        BufferedImage stored = ImageIO.read(jpeg);
        if (stored == null) {
            return false;
        }
        writeJpeg(turn(stored, orientation), jpeg);
        return true;
    }

    private static BufferedImage turn(BufferedImage src, int orientation) {
        int w = src.getWidth(), h = src.getHeight();
        boolean swap = orientation >= 5;
        BufferedImage out = new BufferedImage(swap ? h : w, swap ? w : h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = src.getRGB(x, y);
                switch (orientation) {
                    case 2 -> out.setRGB(w - 1 - x, y, rgb);          // mirrored
                    case 3 -> out.setRGB(w - 1 - x, h - 1 - y, rgb);  // upside down
                    case 4 -> out.setRGB(x, h - 1 - y, rgb);          // mirrored upside down
                    case 5 -> out.setRGB(y, x, rgb);                  // mirrored, turned
                    case 6 -> out.setRGB(h - 1 - y, x, rgb);          // turn 90 clockwise
                    case 7 -> out.setRGB(h - 1 - y, w - 1 - x, rgb);  // mirrored, turned
                    case 8 -> out.setRGB(y, w - 1 - x, rgb);          // turn 90 counterclockwise
                    default -> out.setRGB(x, y, rgb);
                }
            }
        }
        return out;
    }

    private static void writeJpeg(BufferedImage image, File target) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam params = writer.getDefaultWriteParam();
        params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        params.setCompressionQuality(0.95f);
        try (ImageOutputStream out = ImageIO.createImageOutputStream(target)) {
            writer.setOutput(out);
            writer.write(null, new IIOImage(image, null, null), params);
        } finally {
            writer.dispose();
        }
    }
}
