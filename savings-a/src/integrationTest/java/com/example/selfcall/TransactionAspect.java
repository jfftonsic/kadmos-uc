package com.example.selfcall;

import static lombok.AccessLevel.PRIVATE;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * I'm using LoadTime Weaver aspectj for this because trying to put aspect using just spring AOP on the classes that I
 * was trying to, caused some collateral effects such as, for example, the logger of JpaTransactionManager being null
 * and resulting in NPE as if the class haven't been initialized as expected...
 */
@Aspect
@Slf4j
@Getter
public class TransactionAspect {

    @SuperBuilder
    @FieldDefaults(level = PRIVATE, makeFinal = true)
    @Getter @Setter @ToString @EqualsAndHashCode
    public static class TransactionEvent {
        String className;
        String methodName;
        String pointCutMethodName;
    }

    ConcurrentHashMap<Long, List<TransactionEvent>> sequentialCallsByThread = new ConcurrentHashMap<>(8);

    @Before("execution(com.example.selfcall.TransactionalSelfCallTest.new(..)) && target(test)")
    public void injectAspectOnTestInstance(TransactionalSelfCallTest test) {
        sequentialCallsByThread.clear();
        test.transactionAspect = this;
    }


    @Around("execution(* org.springframework.orm.jpa.JpaTransactionManager.doBegin (..)) "
            + "|| execution(* org.springframework.orm.jpa.JpaTransactionManager.doCommit (..)) "
            + "|| execution(* org.springframework.orm.jpa.JpaTransactionManager.doRollback (..)) ")
    public Object doBegin(ProceedingJoinPoint pjp) throws Throwable {


        final var e = new Exception();
        final var first = Arrays.stream(e.getStackTrace())
                .filter(stackTraceElement -> stackTraceElement.getClassName().contains("com.example.selfcall.TransactionalSelfCallService"))
                .findFirst();

        if (first.isPresent()) {
            final var stackTraceElement = first.get();
            final TransactionEvent transactionEvent = TransactionEvent.builder()
                    .className(stackTraceElement.getClassName())
                    .methodName(stackTraceElement.getMethodName())
                    .pointCutMethodName(pjp.getSignature().getName())
                    .build();


            final var transactionEventsForThisThread = sequentialCallsByThread.computeIfAbsent(Thread.currentThread()
                            .getId(),
                    x -> new LinkedList<>());
            transactionEventsForThisThread.add(transactionEvent);
        }

        return pjp.proceed();
    }

}
