package com.example.util;

import org.springframework.context.ApplicationContext;

@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
public interface SelfReferential<T extends SelfReferential> {
    void setSelf(T itself);

    T getSelf();

    default T internalGetSelf(ApplicationContext context, T currSelf) {
        if (currSelf == null) {
            final var actualSelf = (T) context.getBean(this.getClass());
            setSelf(actualSelf);
            return actualSelf;
        } else {
            return currSelf;
        }
    }
}
