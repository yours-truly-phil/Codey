package io.horrorshow.codey.util;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;


@Slf4j
public class DoOr {

    public static <T> T getNonNullOrDefault(Supplier<T> supplier, @NotNull T defaultValue) {
        try {
            var e = supplier.get();
            return Objects.requireNonNullElse(e, defaultValue);
        } catch (Exception e) {
            log.error("Error getting value", e);
        }
        return defaultValue;
    }


    public static <T> T getOrThrow(Supplier<T> supplier, String errMsg) {
        var t = supplier.get();
        if (t != null) {
            return t;
        } else {
            throw new IllegalStateException(errMsg);
        }
    }

}
