/**
 * MicroScript — The programming language
 * Copyright (c) 2025-2026 Cyril John Magayaga
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
 * Extended with @map and @__globalfn__ syntax support
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
    
    // Process @map syntax: @map => (operation) [list]
    public static List<Object> processMap(String operation, List<Object> list) {
        if (operation.startsWith("(*")) {
            // Multiplication operation: (*2)
            String multiplierStr = operation.substring(2, operation.length() - 1).trim();
            try {
                double multiplier = Double.parseDouble(multiplierStr);
                return map(x -> multiplyNumbers(x, multiplier), list);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid multiplier in @map operation: " + operation);
            }
        } else if (operation.startsWith("(+")) {
            // Addition operation: (+1)
            String addendStr = operation.substring(2, operation.length() - 1).trim();
            try {
                double addend = Double.parseDouble(addendStr);
                return map(x -> addNumbers(x, addend), list);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid addend in @map operation: " + operation);
            }
        } else if (operation.startsWith("(-")) {
            // Subtraction operation: (-5)
            String subtrahendStr = operation.substring(2, operation.length() - 1).trim();
            try {
                double subtrahend = Double.parseDouble(subtrahendStr);
                return map(x -> subtractNumbers(x, subtrahend), list);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid subtrahend in @map operation: " + operation);
            }
        } else if (operation.startsWith("(/")) {
            // Division operation: (/2)
            String divisorStr = operation.substring(2, operation.length() - 1).trim();
            try {
                double divisor = Double.parseDouble(divisorStr);
                if (Math.abs(divisor) < 0.0001) {
                    throw new RuntimeException("Division by zero in @map operation: " + operation);
                }
                return map(x -> divideNumbers(x, divisor), list);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid divisor in @map operation: " + operation);
            }
        } else {
            throw new RuntimeException("Unsupported @map operation: " + operation);
        }
    }
    
    // Helper methods for arithmetic operations
    private static Object multiplyNumbers(Object a, double multiplier) {
        if (a instanceof Integer) {
            int result = (Integer) a * (int) multiplier;
            return result;
        } else if (a instanceof Long) {
            long result = (Long) a * (long) multiplier;
            return result;
        } else if (a instanceof Float) {
            float result = (Float) a * (float) multiplier;
            return result;
        } else if (a instanceof Double) {
            double result = (Double) a * multiplier;
            return result;
        } else {
            throw new RuntimeException("Cannot multiply non-numeric type: " + a.getClass().getSimpleName());
        }
    }
    
    private static Object addNumbers(Object a, double addend) {
        if (a instanceof Integer) {
            int result = (Integer) a + (int) addend;
            return result;
        } else if (a instanceof Long) {
            long result = (Long) a + (long) addend;
            return result;
        } else if (a instanceof Float) {
            float result = (Float) a + (float) addend;
            return result;
        } else if (a instanceof Double) {
            double result = (Double) a + addend;
            return result;
        } else {
            throw new RuntimeException("Cannot add to non-numeric type: " + a.getClass().getSimpleName());
        }
    }
    
    private static Object subtractNumbers(Object a, double subtrahend) {
        if (a instanceof Integer) {
            int result = (Integer) a - (int) subtrahend;
            return result;
        } else if (a instanceof Long) {
            long result = (Long) a - (long) subtrahend;
            return result;
        } else if (a instanceof Float) {
            float result = (Float) a - (float) subtrahend;
            return result;
        } else if (a instanceof Double) {
            double result = (Double) a - subtrahend;
            return result;
        } else {
            throw new RuntimeException("Cannot subtract from non-numeric type: " + a.getClass().getSimpleName());
        }
    }
    
    private static Object divideNumbers(Object a, double divisor) {
        if (a instanceof Integer) {
            int intA = (Integer) a;
            int intDivisor = (int) divisor;
            if (intA % intDivisor == 0) {
                return intA / intDivisor;
            } else {
                return (double) intA / intDivisor;
            }
        } else if (a instanceof Long) {
            long longA = (Long) a;
            long longDivisor = (long) divisor;
            if (longA % longDivisor == 0) {
                return longA / longDivisor;
            } else {
                return (double) longA / longDivisor;
            }
        } else if (a instanceof Float) {
            return (Float) a / (float) divisor;
        } else if (a instanceof Double) {
            return (Double) a / divisor;
        } else {
            throw new RuntimeException("Cannot divide non-numeric type: " + a.getClass().getSimpleName());
        }
    }
}
