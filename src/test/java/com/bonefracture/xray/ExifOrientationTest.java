package com.bonefracture.xray;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExifOrientationTest {

    // 200 x 100, left half red, right half blue
    private static BufferedImage twoColours(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, width / 2, height);
        g.setColor(Color.BLUE);
        g.fillRect(width / 2, 0, width - width / 2, height);
        g.dispose();
        return image;
    }

    private static boolean reddish(int rgb) {
        return ((rgb >> 16) & 0xFF) > 150 && (rgb & 0xFF) < 100;
    }

    private static boolean bluish(int rgb) {
        return (rgb & 0xFF) > 150 && ((rgb >> 16) & 0xFF) < 100;
    }

    @Test
    void readsTheOrientationTag() throws IOException {
        assertEquals(6, ExifOrientation.read(PhonePhoto.jpeg(twoColours(200, 100), 6)));
        assertEquals(3, ExifOrientation.read(PhonePhoto.jpeg(twoColours(200, 100), 3)));
    }

    @Test
    void withoutExifThePhotoIsUpright() throws IOException {
        ByteArrayOutputStream plain = new ByteArrayOutputStream();
        ImageIO.write(twoColours(200, 100), "jpg", plain);
        assertEquals(1, ExifOrientation.read(plain.toByteArray()));
        assertEquals(1, ExifOrientation.read(new byte[]{1, 2, 3}));
    }

    @Test
    void sidewaysPhotoIsTurnedUpright(@TempDir Path dir) throws IOException {
        // stored 200 x 100, red left; orientation 6 = turn 90 degrees clockwise to show it
        File photo = dir.resolve("phone.jpg").toFile();
        Files.write(photo.toPath(), PhonePhoto.jpeg(twoColours(200, 100), 6));

        assertTrue(ExifOrientation.makeUpright(photo));

        BufferedImage upright = ImageIO.read(photo);
        assertEquals(100, upright.getWidth());
        assertEquals(200, upright.getHeight());
        assertTrue(reddish(upright.getRGB(50, 20)), "red ends up at the top");
        assertTrue(bluish(upright.getRGB(50, 180)), "blue ends up at the bottom");
        assertEquals(1, ExifOrientation.read(Files.readAllBytes(photo.toPath())));
    }

    @Test
    void upsideDownPhotoIsTurned(@TempDir Path dir) throws IOException {
        File photo = dir.resolve("phone.jpg").toFile();
        Files.write(photo.toPath(), PhonePhoto.jpeg(twoColours(200, 100), 3));

        assertTrue(ExifOrientation.makeUpright(photo));

        BufferedImage upright = ImageIO.read(photo);
        assertEquals(200, upright.getWidth());
        assertTrue(bluish(upright.getRGB(20, 50)), "blue ends up on the left");
        assertTrue(reddish(upright.getRGB(180, 50)));
    }

    @Test
    void uprightPhotoIsLeftAsItIs(@TempDir Path dir) throws IOException {
        File photo = dir.resolve("phone.jpg").toFile();
        byte[] bytes = PhonePhoto.jpeg(twoColours(200, 100), 1);
        Files.write(photo.toPath(), bytes);

        assertFalse(ExifOrientation.makeUpright(photo));
        assertArrayEquals(bytes, Files.readAllBytes(photo.toPath()));
    }
}
