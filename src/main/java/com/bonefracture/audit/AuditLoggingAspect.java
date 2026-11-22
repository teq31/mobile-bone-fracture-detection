package com.bonefracture.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.logging.Logger;

/**
 * AOP: records who did what with the accounts (register, login, doctor verification),
 * so UserService and its implementations contain no audit code. Passwords are never recorded.
 *
 * Woven at compile time by the AspectJ compiler (aspectj-maven-plugin).
 */
@Aspect
public class AuditLoggingAspect {

    private static final Logger LOGGER = Logger.getLogger("audit");

    private final AuditLog auditLog = new AuditLog();

    @Pointcut("execution(* com.bonefracture.user.UserService+.register(..)) && args(role, name, email, password)")
    void register(Object role, String name, String email, String password) {
    }

    @Pointcut("execution(boolean com.bonefracture.user.UserService+.login(..)) && args(email, password)")
    void login(String email, String password) {
    }

    @Pointcut("execution(* com.bonefracture.user.UserService+.verifyDoctor(..)) && args(doctorEmail)")
    void verifyDoctor(String doctorEmail) {
    }

    @Around(value = "register(role, name, email, password)", argNames = "joinPoint,role,name,email,password")
    public Object auditRegister(ProceedingJoinPoint joinPoint, Object role, String name, String email,
                                String password) throws Throwable {
        return audit(joinPoint, email, "REGISTER", String.valueOf(role));
    }

    @Around(value = "login(email, password)", argNames = "joinPoint,email,password")
    public Object auditLogin(ProceedingJoinPoint joinPoint, String email, String password) throws Throwable {
        boolean success = false;
        try {
            success = (boolean) joinPoint.proceed();
            return success;
        } finally {
            save(email, "LOGIN", email, success);
        }
    }

    @Around(value = "verifyDoctor(doctorEmail)", argNames = "joinPoint,doctorEmail")
    public Object auditVerifyDoctor(ProceedingJoinPoint joinPoint, String doctorEmail) throws Throwable {
        return audit(joinPoint, currentUser(), "VERIFY_DOCTOR", doctorEmail);
    }

    private Object audit(ProceedingJoinPoint joinPoint, String actor, String action, String target) throws Throwable {
        boolean success = false;
        try {
            Object result = joinPoint.proceed();
            success = true;
            return result;
        } finally {
            save(actor, action, target, success);
        }
    }

    private void save(String actor, String action, String target, boolean success) {
        AuditEntry entry = new AuditEntry(Instant.now(), actor, action, target, success);
        auditLog.record(entry);
        LOGGER.info(action + " by " + actor + " on " + target + (success ? " ok" : " failed"));
    }

    // The logged-in user: the email from the JWT, or "system" for actions without a user (e.g. at startup)
    private static String currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return "system";
        }
        if (authentication.getPrincipal() instanceof Jwt jwt && jwt.getClaimAsString("email") != null) {
            return jwt.getClaimAsString("email");
        }
        return authentication.getName();
    }

    public AuditLog getAuditLog() {
        return auditLog;
    }
}
