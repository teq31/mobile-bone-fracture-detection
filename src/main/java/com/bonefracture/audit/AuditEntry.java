package com.bonefracture.audit;

import java.time.Instant;

// One user action: who did what, on what, when and if it worked
public record AuditEntry(Instant time, String actor, String action, String target, boolean success) {
}
