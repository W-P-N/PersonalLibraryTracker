package com.wpn.personallibrarytracker.utility;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger LOGGER = LogManager.getLogger(LoggingAspect.class);
    @Around("execution(* com.wpn..*Impl.*(..))")
    public Object aroundLogging(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        LOGGER.info("Entering: " + className + " - " + methodName);
        Object result;
        long start = System.nanoTime();
        try {
            result = joinPoint.proceed();
            long duration = (System.nanoTime() - start) / 1_000_000;
            LOGGER.info("Execution Time: " + duration);
        } catch(Throwable throwable) {
            LOGGER.error(throwable.getMessage());
            throw throwable;
        }
        LOGGER.info("Exiting: " + className + " - " + methodName);
        return result;
    }
}
