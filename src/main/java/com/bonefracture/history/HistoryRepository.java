package com.bonefracture.history;

import com.bonefracture.user.User;
import com.bonefracture.xray.XRayImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Facade pattern - subsystem class
// Saves and loads the X-ray history of a user (kept in memory for now; later SQLite / PostgreSQL).
public class HistoryRepository {

    private Map<User, List<XRayImage>> historyByUser = new HashMap<>();

    public void save(User user, XRayImage xray) {
        List<XRayImage> history = historyByUser.computeIfAbsent(user, u -> new ArrayList<>());
        if (!history.contains(xray)) {
            history.add(xray);
        }
    }

    public List<XRayImage> findByUser(User user) {
        return new ArrayList<>(historyByUser.getOrDefault(user, List.of()));
    }
}
