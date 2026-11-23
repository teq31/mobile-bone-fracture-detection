package com.bonefracture.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Arrays;
import java.util.logging.Logger;

@Aspect
public class ConfigurationAuditAspect {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationAuditAspect.class.getName());

    @Pointcut("execution(* com.bonefracture.service.ConfigurationService.load*(..))")
    public void configurationLoads() {
    }

    @AfterReturning("configurationLoads()")
    public void logSuccessfulLoad(JoinPoint joinPoint) {
        LOGGER.info("Configuration loaded via " + joinPoint.getSignature().getName()
                + describeArguments(joinPoint));
    }

    @AfterThrowing(pointcut = "configurationLoads()", throwing = "error")
    public void logFailedLoad(JoinPoint joinPoint, Throwable error) {
        LOGGER.warning("Configuration load failed via " + joinPoint.getSignature().getName()
                + describeArguments(joinPoint) + ": " + error.getMessage());
    }

    private String describeArguments(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length == 0) {
            return "";
        }
        return " " + Arrays.toString(args);
    }
}
