package com.bonefracture.cache;

import com.bonefracture.xray.XRayImage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Builds cache keys. An X-ray image is identified by the SHA-256 hash of its file content,
 * so the same image uploaded twice (even under another file name) gets the same key.
 */
public final class CacheKeys {

    private CacheKeys() {
    }

    /** Hash of the image file content; empty if the file cannot be read (then nothing is cached). */
    public static Optional<String> imageHash(XRayImage image) {
        if (image == null || image.getPath() == null) {
            return Optional.empty();
        }
        try {
            Path file = Path.of(image.getPath());
            if (!Files.isRegularFile(file)) {
                return Optional.empty();
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            return Optional.of(HexFormat.of().formatHex(digest.digest()));
        } catch (IOException | InvalidPathException e) {
            return Optional.empty();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Key from the method and its arguments; empty if an image argument has no readable content. */
    public static Optional<String> forCall(String method, Object[] args) {
        StringJoiner key = new StringJoiner("|", method + "(", ")");
        for (Object arg : args) {
            if (arg instanceof XRayImage image) {
                Optional<String> hash = imageHash(image);
                if (hash.isEmpty()) {
                    return Optional.empty();
                }
                key.add("image:" + hash.get());
            } else {
                key.add(String.valueOf(arg));
            }
        }
        return Optional.of(key.toString());
    }
}
