package com.bonefracture.history;

import com.bonefracture.user.User;
import com.bonefracture.xray.XRayImage;

import java.util.ArrayList;
import java.util.List;

// Facade pattern - subsystem class
// Saves and loads the X-ray history of a user.
public class HistoryRepository {

    public void save(User user, XRayImage xray) {
        // TODO
    }

    public List<XRayImage> findByUser(User user) {
        // TODO
        return new ArrayList<>();
    }
}
