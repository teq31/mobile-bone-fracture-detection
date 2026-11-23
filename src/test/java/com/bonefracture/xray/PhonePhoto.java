package com.bonefracture.xray;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

// Builds a JPEG with an EXIF block holding only the Orientation tag, like a phone camera does
public final class PhonePhoto {

    private PhonePhoto() {
    }

    public static byte[] jpeg(BufferedImage image, int orientation) throws IOException {
        ByteArrayOutputStream plain = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", plain);
        byte[] jpeg = plain.toByteArray();
        byte[] tiff = {
                'M', 'M', 0, 42, 0, 0, 0, 8,          // big endian, first IFD at offset 8
                0, 1,                                  // one entry
                0x01, 0x12, 0, 3, 0, 0, 0, 1,          // Orientation, SHORT, count 1
                0, (byte) orientation, 0, 0,           // value
                0, 0, 0, 0};                           // no next IFD
        int length = 2 + 6 + tiff.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(jpeg, 0, 2);                         // SOI
        out.write(new byte[]{(byte) 0xFF, (byte) 0xE1, (byte) (length >> 8), (byte) length});
        out.write(new byte[]{'E', 'x', 'i', 'f', 0, 0});
        out.write(tiff);
        out.write(jpeg, 2, jpeg.length - 2);
        return out.toByteArray();
    }
}
