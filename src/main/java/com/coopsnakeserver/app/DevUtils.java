package com.coopsnakeserver.app;

import java.util.Collection;

/**
 * Utils
 *
 * created: 25.04.2024
 *
 * @author June L. Gschwantner
 */
public class DevUtils {
    /**
     * Check an assertion that is required to be true at runtime. Throws a
     * {@code}RuntimeException{@code} if it is not.
     *
     * @param predicat Condition that must be true.
     */
    public static void assertion(boolean predicat) {
        if (!predicat) {
            throw new RuntimeException("ASSERTION FAILED");
        }
    }

    /**
     * Check an assertion that is required to be true at runtime. Throws a
     * {@code}RuntimeException{@code} containing the provided {@code}msg{@code} if
     * it is not.
     *
     *
     * @param predicat Condition that must be true.
     */
    public static void assertion(boolean predicat, String msg) {
        if (!predicat) {
            throw new RuntimeException(String.format("ASSERTION FAILED: %s", msg));
        }
    }
}
