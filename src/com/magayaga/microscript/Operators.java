/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

public class Operators {
    public static int increment(Environment environment, String varName) {
        Object value = environment.getVariable(varName);
        if (value instanceof Integer) {
            int intValue = (Integer) value;
            intValue++;
            environment.setVariable(varName, intValue);
            return intValue;
        }
        
        else {
            throw new RuntimeException("Type error: " + varName + " is not an Integer.");
        }
    }

    public static int decrement(Environment environment, String varName) {
        Object value = environment.getVariable(varName);
        if (value instanceof Integer) {
            int intValue = (Integer) value;
            intValue--;
            environment.setVariable(varName, intValue);
            return intValue;
        }
        
        else {
            throw new RuntimeException("Type error: " + varName + " is not an Integer.");
        }
    }
}