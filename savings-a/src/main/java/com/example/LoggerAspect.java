package com.example;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggerAspect {

    /**
     * Adds trace logging before a proceeding join point method call.
     *
     * @param pjp The proceeding joint point
     * @return Result of method call
     */
    @Around("execution(* com.example.business..*.* (..))")
    public Object logBeforeAndAfterServiceMethods(ProceedingJoinPoint pjp) throws Throwable {
        log.info("{} has started execution.", pjp.getSignature());
        Object resultOfMethodCall = pjp.proceed();
        log.info("{} finished execution", pjp.getSignature());
        return resultOfMethodCall;
    }
}
