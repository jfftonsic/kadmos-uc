package com.example.selfcall;

import com.example.util.SelfReferential;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

/**
 * This service class shows the issues you can get with Spring AOP proxy classes with @Transactional usages.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class TransactionalSelfCallService implements SelfReferential<TransactionalSelfCallService> {
    TransactionalSelfCallService selfPostConstruct;
    TransactionalSelfCallService selfApplicationContext;
    final ApplicationContext applicationContext;

    @PostConstruct
    public void postConstruct() {
        selfPostConstruct = this;
    }

    @Override
    public void setSelf(TransactionalSelfCallService itself) {
        selfApplicationContext = itself;
    }

    @Override
    public TransactionalSelfCallService getSelf() {
        return internalGetSelf(applicationContext, selfApplicationContext);
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
    public void selfPostConstructCall() {
        // requires new on nestedTransaction does not work
        selfPostConstruct.nestedTransaction();
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void selfApplicationContextCall() {
        // requires new on nestedTransaction works in this case
        getSelf().nestedTransaction();
    }
}
