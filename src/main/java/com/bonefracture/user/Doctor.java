package com.bonefracture.user;

import com.bonefracture.chat.ChatMessage;
import com.bonefracture.detection.AnalysisResult;
import com.bonefracture.practice.PracticeAttempt;
import com.bonefracture.xray.XRayImage;

import java.util.ArrayList;
import java.util.List;

public class Doctor extends User {

    private boolean isVerified; // set by admin
    private List<XRayImage> sharedWithMe = new ArrayList<>();

    public Doctor(String name, String email) {
        super(name, email);
    }

    public PracticeAttempt practice(XRayImage xray) {
        return null;
    }

    public ChatMessage askAssistant(String question) {
        return null;
    }

    public List<AnalysisResult> compareModels(XRayImage xray) {
        return new ArrayList<>();
    }

    public List<XRayImage> viewPatientXRays() {
        return sharedWithMe;
    }

    public boolean isVerified() { return isVerified; }

    void receiveXRay(XRayImage xray) {
        sharedWithMe.add(xray);
    }

    void setVerified(boolean verified) {
        isVerified = verified;
    }
}
