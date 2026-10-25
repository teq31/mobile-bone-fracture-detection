package com.bonefracture.user;

import com.bonefracture.chat.ChatMessage;
import com.bonefracture.practice.PracticeAttempt;
import com.bonefracture.xray.XRayImage;

public class Student extends User {

    public Student(String name, String email) {
        super(name, email);
    }

    public PracticeAttempt practice(XRayImage xray) {
        return null;
    }

    public ChatMessage askAssistant(String question) {
        return null;
    }
}
