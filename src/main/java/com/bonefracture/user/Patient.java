package com.bonefracture.user;

import com.bonefracture.chat.ChatMessage;
import com.bonefracture.xray.XRayImage;

public class Patient extends User {

    public Patient(String name, String email) {
        super(name, email);
    }

    public ChatMessage askAssistant(String question) {
        return null;
    }

    public void shareXRay(XRayImage xray, Doctor doctor) {
        doctor.receiveXRay(xray);
    }
}
