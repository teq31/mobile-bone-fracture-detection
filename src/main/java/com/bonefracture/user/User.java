package com.bonefracture.user;

import com.bonefracture.chat.ChatMessage;
import com.bonefracture.practice.PracticeAttempt;
import com.bonefracture.xray.XRayImage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public abstract class User {

    protected String name;
    protected String email;
    protected String passwordHash;
    protected List<XRayImage> history = new ArrayList<>();
    protected List<ChatMessage> messages = new ArrayList<>();
    protected List<PracticeAttempt> attempts = new ArrayList<>();

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void signUp(String password) {
        passwordHash = hash(password);
    }

    public boolean login(String password) {
        return passwordHash != null && passwordHash.equals(hash(password));
    }

    private static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public XRayImage uploadXRay(File file) {
        return null;
    }

    public List<XRayImage> viewHistory() {
        return history;
    }

    String getPasswordHash() {
        return passwordHash;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
}
