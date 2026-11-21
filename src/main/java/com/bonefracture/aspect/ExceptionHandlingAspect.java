package com.bonefracture.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class ExceptionHandlingAspect {

    @Pointcut("execution(* com.bonefracture.service..*Service.*(..))")
    public void serviceMethods() {
    }

    @Pointcut("execution(* com.bonefracture.api..*Controller.*(..))")
    public void controllerMethods() {
    }

    @Around("serviceMethods()")
    public Object handleServiceErrors(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (ServiceException alreadyHandled) {
            throw alreadyHandled;
        } catch (Throwable error) {
            throw new ServiceException(operationName(joinPoint), buildMessage(joinPoint, error), error);
        }
    }

    @Around("controllerMethods()")
    public Object handleControllerErrors(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable error) {
            if (isHandledElsewhere(error)) {
                throw error;
            }
            throw new ServiceException(operationName(joinPoint), buildMessage(joinPoint, error), error);
        }
    }

    private boolean isHandledElsewhere(Throwable error) {
        if (error instanceof ServiceException) {
            return true;
        }
        String type = error.getClass().getName();
        return type.startsWith("org.springframework.")
                || error instanceof IllegalArgumentException
                || error instanceof IllegalStateException;
    }

    private String operationName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }

    private String buildMessage(ProceedingJoinPoint joinPoint, Throwable error) {
        String reason = (error.getMessage() == null || error.getMessage().isBlank())
                ? error.getClass().getSimpleName()
                : error.getMessage();
        return "Operation '" + operationName(joinPoint) + "' failed: " + reason;
    }
}
