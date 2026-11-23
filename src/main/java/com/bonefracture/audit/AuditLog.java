package com.bonefracture.audit;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

// The last user actions, newest first. Kept in memory: the server log has the full history.
public class AuditLog {

    private static final int MAX_ENTRIES = 1000;

    private final Deque<AuditEntry> entries = new ConcurrentLinkedDeque<>();

    public void record(AuditEntry entry) {
        entries.addFirst(entry);
        while (entries.size() > MAX_ENTRIES) {
            entries.pollLast();
        }
    }

    public List<AuditEntry> entries() {
        return new ArrayList<>(entries);
    }

    public void clear() {
        entries.clear();
    }
}
