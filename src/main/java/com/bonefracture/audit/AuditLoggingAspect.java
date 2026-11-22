package com.bonefracture.audit;

import org.aspectj.lang.annotation.Aspect;

@Aspect
public class AuditLoggingAspect {

    private final AuditLog auditLog = new AuditLog();

    public AuditLog getAuditLog() {
        return auditLog;
    }
}
