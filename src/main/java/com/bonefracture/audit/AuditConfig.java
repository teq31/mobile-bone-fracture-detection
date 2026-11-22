package com.bonefracture.audit;

import org.aspectj.lang.Aspects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditConfig {

    // the aspect is created by AspectJ, not by Spring; its log is shared with the admin endpoint
    @Bean
    public AuditLog auditLog() {
        return Aspects.aspectOf(AuditLoggingAspect.class).getAuditLog();
    }
}
