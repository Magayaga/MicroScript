/**
 * MicroScript — The programming language
 * Copyright (c) 2024 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, Function> functions = new HashMap<>();
    private final Environment parent;

    public Environment() {
        this.parent = null;
    }

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    public Object getVariable(String name) {
        Object value = variables.get(name);
        if (value == null && parent != null) {
            return parent.getVariable(name);
        }
        return value;
    }

    public void defineFunction(Function function) {
        functions.put(function.getName(), function);
    }

    public Function getFunction(String name) {
        Function function = functions.get(name);
        if (function == null && parent != null) {
            return parent.getFunction(name);
        }
        return function;
    }
}
