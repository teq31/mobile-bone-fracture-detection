package com.bonefracture.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.logging.Logger;

@Aspect
public class LoggingAspect {

    private static final Logger LOGGER = Logger.getLogger(LoggingAspect.class.getName());

    @Pointcut("execution(* com.bonefracture.api..*Controller.*(..))")
    public void controllerMethods() {
    }

    @Around("controllerMethods()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        String operation = operationName(joinPoint);
        long start = System.nanoTime();
        LOGGER.info("-> " + operation);
        try {
            Object result = joinPoint.proceed();
            LOGGER.info("<- " + operation + " (" + elapsedMillis(start) + " ms)");
            return result;
        } catch (Throwable error) {
            LOGGER.warning("x- " + operation + " (" + elapsedMillis(start) + " ms): "
                    + error.getClass().getSimpleName());
            throw error;
        }
    }

    private String operationName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
