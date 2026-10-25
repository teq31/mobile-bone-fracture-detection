package com.bonefracture.user;

import com.bonefracture.chat.ChatMessage;
import com.bonefracture.practice.PracticeAttempt;
import com.bonefracture.xray.XRayImage;

import java.io.File;
import java.util.ArrayList;
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
        // TODO hash + save
    }

    public boolean login(String password) {
        return false;
    }

    public XRayImage uploadXRay(File file) {
        return null;
    }

    public List<XRayImage> viewHistory() {
        return history;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
}
