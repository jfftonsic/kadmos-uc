package com.example.selfcall;

import com.example.util.SelfReferential2;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * This service class shows the issues you can get with Spring AOP proxy classes with @Transactional usages.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class TransactionalSelfCallService2 implements SelfReferential2<TransactionalSelfCallService2> {

    TransactionalSelfCallService2 selfApplicationContext;

    @Override
    public void setSelf(TransactionalSelfCallService2 itself) {
        selfApplicationContext = itself;
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void nestedTransaction() {

    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void directCall() {
        // requires new on nestedTransaction does not work
        nestedTransaction();
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void selfApplicationContextCall() {
        // requires new on nestedTransaction works in this case
        selfApplicationContext.nestedTransaction();
    }
}
