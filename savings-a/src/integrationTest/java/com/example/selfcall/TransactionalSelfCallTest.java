package com.example.selfcall;

import com.example.util.FakeClockConfiguration;
import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

@Configuration
@EnableLoadTimeWeaving(aspectjWeaving = EnableLoadTimeWeaving.AspectJWeaving.ENABLED)
class Config {
    @Bean
    public TransactionalSelfCallService transactionalSelfCallService(ApplicationContext applicationContext) {
        return new TransactionalSelfCallService(applicationContext);
    }

    @Bean
    public TransactionalSelfCallService2 transactionalSelfCallService2(ApplicationContext applicationContext) {
        return new TransactionalSelfCallService2();
    }
}


@ActiveProfiles("integrationTest")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ FakeClockConfiguration.class, Config.class })
@EnableAspectJAutoProxy
@Slf4j
@TestPropertySource(properties = {
        "spring.datasource.hikari.auto-commit=false",
        "logging.level.org.hibernate.resource.jdbc.internal=INFO",
        "logging.level.org.hibernate.SQL=TRACE",
        "logging.level.org.hibernate.type.descriptor.sql=INFO",
})
public class TransactionalSelfCallTest {

    @Autowired
    public TransactionalSelfCallService service;

    @Autowired
    public TransactionalSelfCallService2 service2;

    TransactionAspect transactionAspect;

    @Test
    @DisplayName("""
            Calling a method from the same class directly bypasses Spring AOP proxy, an thus, doesn't genereta a new
            transaction when calling the 'nestedTransaction' method.
            """)
    public void directCall() {
        service.directCall();

        final var transactionEvents = transactionAspect.getSequentialCallsByThread()
                .get(Thread.currentThread().getId());
        assertNotNull(transactionEvents);
        assertEquals(2, transactionEvents.size());

        checkTransactionalCall(transactionEvents, 0, "doBegin", "directCall");
        checkTransactionalCall(transactionEvents, 1, "doCommit", "directCall");
    }

    @Test
    @DisplayName("""
            Self reference by trying to set a field with 'this' at the @PostConstruct fails to use the requires new of
            the 'nestedTransaction' method.
            """)
    public void selfPostConstructCall() {
        service.selfPostConstructCall();

        final var transactionEvents = transactionAspect.getSequentialCallsByThread()
                .get(Thread.currentThread().getId());
        assertNotNull(transactionEvents);
        assertEquals(2, transactionEvents.size());

        checkTransactionalCall(transactionEvents, 0, "doBegin", "selfPostConstructCall");
        checkTransactionalCall(transactionEvents, 1, "doCommit", "selfPostConstructCall");

    }

    @Test
    @DisplayName("""
            Self reference by getting the bean from spring's context. Here, the requires new of the nested transaction
            works, because spring returns for you the proxy instance and not your real 'this'.
            """)
    public void selfApplicationContextCall() {
        service.selfApplicationContextCall();

        final var transactionEvents = transactionAspect.getSequentialCallsByThread()
                .get(Thread.currentThread().getId());
        assertNotNull(transactionEvents);
        assertEquals(4, transactionEvents.size());

        checkTransactionalCall(transactionEvents, 0, "doBegin", "selfApplicationContextCall");
        checkTransactionalCall(transactionEvents, 1, "doBegin", "nestedTransaction");
        checkTransactionalCall(transactionEvents, 2, "doCommit", "nestedTransaction");
        checkTransactionalCall(transactionEvents, 3, "doCommit", "selfApplicationContextCall");

    }

    @Test
    @DisplayName("""
            Second implementation of self reference interface.
            Calling a method from the same class directly bypasses Spring AOP proxy, an thus, doesn't genereta a new
            transaction when calling the 'nestedTransaction' method.
            """)
    public void directCall2() {
        service2.directCall();

        final var transactionEvents = transactionAspect.getSequentialCallsByThread()
                .get(Thread.currentThread().getId());
        assertNotNull(transactionEvents);
        assertEquals(2, transactionEvents.size());

        checkTransactionalCall(transactionEvents, 0, "doBegin", "directCall");
        checkTransactionalCall(transactionEvents, 1, "doCommit", "directCall");
    }

    @Test
    @DisplayName("""
            Second implementation of self reference interface.
            Self reference by getting the bean from spring's context. Here, the requires new of the nested transaction
            works, because spring returns for you the proxy instance and not your real 'this'.
            """)
    public void selfApplicationContextCall2() {
        service2.selfApplicationContextCall();

        final var transactionEvents = transactionAspect.getSequentialCallsByThread()
                .get(Thread.currentThread().getId());
        assertNotNull(transactionEvents);
        assertEquals(4, transactionEvents.size());

        checkTransactionalCall(transactionEvents, 0, "doBegin", "selfApplicationContextCall");
        checkTransactionalCall(transactionEvents, 1, "doBegin", "nestedTransaction");
        checkTransactionalCall(transactionEvents, 2, "doCommit", "nestedTransaction");
        checkTransactionalCall(transactionEvents, 3, "doCommit", "selfApplicationContextCall");

    }

    private void checkTransactionalCall(List<TransactionAspect.TransactionEvent> transactionEvents, int i,
            String pointCutMethodName, String serviceMethodName) {
        final var transactionEvent1 = transactionEvents.get(i);
        assertEquals(serviceMethodName, transactionEvent1.getMethodName());
        assertEquals(pointCutMethodName, transactionEvent1.getPointCutMethodName());
    }
}
