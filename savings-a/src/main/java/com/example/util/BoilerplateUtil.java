package com.example.util;

import java.util.List;
import java.util.Optional;

public class BoilerplateUtil {


    public static <T> T getExactlyOne(List<T> l, String what) {
        if (l.isEmpty()) {
            throw new IllegalStateException("No %s.".formatted(what));
        } else if (l.size() > 1) {
            throw new IllegalStateException("Should exist only one %s.".formatted(what));
        } else {
            return l.get(0);
        }
    }

    public static <T> T getExactlyOne(Optional<T> o, String what) {
        if (o.isEmpty()) {
            throw new IllegalStateException("No %s on database.".formatted(what));
        } else {
            return o.get();
        }
    }

}
