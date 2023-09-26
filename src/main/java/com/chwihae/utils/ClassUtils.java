package com.chwihae.utils;

import java.util.Objects;

public abstract class ClassUtils {
    public static <T> T getSafeCastInstance(Object o, Class<T> clazz) {
        if (Objects.nonNull(clazz) && clazz.isInstance(o)) {
            return clazz.cast(o);
        }
        return null;
    }
}
