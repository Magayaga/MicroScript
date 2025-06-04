/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * FunctionHigherOrder — Haskell-style higher-order functions for MicroScript
 * Supports: map, filter, foldlt (fold left), foldrt (fold right)
 */
public class FunctionHigherOrder {
    // Applies a function to each element of the list and returns a new list
    public static List<Object> map(Function<Object, Object> fn, List<Object> list) {
        List<Object> result = new ArrayList<>();
        for (Object item : list) {
            result.add(fn.apply(item));
        }
        return result;
    }

    // Returns a new list containing only elements for which the predicate returns true
    public static List<Object> filter(Function<Object, Boolean> predicate, List<Object> list) {
        List<Object> result = new ArrayList<>();
        for (Object item : list) {
            if (predicate.apply(item)) {
                result.add(item);
            }
        }
        return result;
    }

    // Left fold: foldlt(fn, initial, list)
    public static Object foldlt(BiFunction<Object, Object, Object> fn, Object initial, List<Object> list) {
        Object acc = initial;
        for (Object item : list) {
            acc = fn.apply(acc, item);
        }
        return acc;
    }

    // Right fold: foldrt(fn, initial, list)
    public static Object foldrt(BiFunction<Object, Object, Object> fn, Object initial, List<Object> list) {
        Object acc = initial;
        for (int i = list.size() - 1; i >= 0; i--) {
            acc = fn.apply(list.get(i), acc);
        }
        return acc;
    }
}
