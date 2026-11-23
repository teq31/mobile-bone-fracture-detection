package com.bonefracture.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class LoginThrottlingAspect {

    private static final LoginAttemptLimiter LIMITER = new LoginAttemptLimiter();

    @Around("execution(* com.bonefracture.auth.AuthService.login(String, String)) && args(email, password)")
    public Object throttleLogin(ProceedingJoinPoint joinPoint, String email, String password) throws Throwable {
        LIMITER.checkNotLocked(email);
        try {
            Object result = joinPoint.proceed();
            LIMITER.recordSuccess(email);
            return result;
        } catch (Throwable error) {
            LIMITER.recordFailure(email);
            throw error;
        }
    }
}
