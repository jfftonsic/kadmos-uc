package com.example;

@FunctionalInterface
public interface ExceptionalSupplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    T get() throws Exception;
}
