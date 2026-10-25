package com.bonefracture.user;

import com.bonefracture.detection.DetectionModel;

import java.util.List;

public class Administrator extends User {

    public Administrator(String name, String email) {
        super(name, email);
    }

    public void verifyDoctor(Doctor doctor) {
        doctor.setVerified(true);
    }

    public void manageUsersAndRoles() {
    }

    public void manageModelVersions(List<DetectionModel> models) {
    }
}
