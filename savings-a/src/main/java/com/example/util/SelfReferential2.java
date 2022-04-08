package com.example.util;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * Another way to implement self-reference but I think it may be possible to happen the following edge case:
 * Some method of the class, that uses the 'self', be called before the event listener methods gets the opportunity
 * to set the 'self' field.
 * @param <T>
 */
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
public interface SelfReferential2<T extends SelfReferential2> {
    void setSelf(T itself);

    @EventListener
    default void handleContextRefreshEvent(ContextRefreshedEvent event) {
        final var actualSelf = event.getApplicationContext().getBean((Class<T>) this.getClass());
        setSelf(actualSelf);
    }

}
