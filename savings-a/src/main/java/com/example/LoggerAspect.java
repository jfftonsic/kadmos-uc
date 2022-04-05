package com.example;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class LoggerAspect implements ApplicationListener<ContextStoppedEvent> {
    ConcurrentHashMap<Long, Stack<String>> m = new ConcurrentHashMap<>();

    /**
     * Adds trace logging before a proceeding join point method call.
     *
     * @param pjp The proceeding joint point
     * @return Result of method call
     */
    @Around("execution(* com.example.business..*.* (..))")
    public Object logBeforeAndAfterMethods(ProceedingJoinPoint pjp) throws Throwable {
        final var signature = pjp.getSignature();
        final var tid = Thread.currentThread().getId();
        final var strings = m.computeIfAbsent(tid, t -> new Stack<>());

        log.info("\n\nSTART {} args {}\n\n", pjp.getSignature(), args(pjp));
        Object resultOfMethodCall = pjp.proceed();
        log.info("\n\nEND {}\n\n", signature);
        return resultOfMethodCall;
    }

    private String args(ProceedingJoinPoint pjp) {
        final var args = pjp.getArgs();
        final var argsStr = Arrays.stream(args).map(o -> {

            if (
                    o instanceof Integer
                            || o instanceof Long
                            || o instanceof Float
                            || o instanceof Double
                            || o instanceof Character
                            || o instanceof String
                            || o instanceof Byte
                            || o instanceof BigDecimal
                            || o instanceof UUID
                            || o instanceof Temporal
            ) {
                return o.toString();
            } else {
                return o.getClass().getSimpleName();
            }
        }).collect(Collectors.joining(", "));
//        log.info("Parameters: {}", argsStr);
        return argsStr;
    }

    @Override
    public void onApplicationEvent(ContextStoppedEvent event) {
        log.info("############ CONTEXT STOPPED EVENT #################");
    }
}
