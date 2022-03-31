package com.example.util;

public class LambdaUtil {

    public static <R> R apply(ExceptionCallable<R> f) {
        try {
            return f.apply();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void run(ExceptionRunnable f) {
        try {
            f.run();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public interface ExceptionCallable<R> {
        R apply() throws Throwable;
    }


    public interface ExceptionRunnable {
        void run() throws Throwable;
    }
}
