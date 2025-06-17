/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class Environment {
    private final Map<String, Object> variables;
    private final Map<String, Function> functions;
    private final Map<String, Struct> structs;
    private final Set<String> immutableVariables;
    private final Environment parent;

    public Environment() {
        this.variables = new HashMap<>();
        this.functions = new HashMap<>();
        this.structs = new HashMap<>();
        this.immutableVariables = new HashSet<>();
        this.parent = null;
    }

    public Environment(Environment parent) {
        this.variables = new HashMap<>();
        this.functions = new HashMap<>();
        this.structs = new HashMap<>();
        this.immutableVariables = new HashSet<>();
        this.parent = parent;
    }

    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    public void setImmutableVariable(String name, Object value) {
        variables.put(name, value);
        immutableVariables.add(name);
    }

    public boolean isImmutable(String name) {
        if (immutableVariables.contains(name)) {
            return true;
        }
        if (parent != null) {
            return parent.isImmutable(name);
        }
        return false;
    }

    public Object getVariable(String name) {
        Object value = variables.get(name);
        if (value != null) {
            return value;
        }
        if (parent != null) {
            return parent.getVariable(name);
        }
        return null;
    }

    public void defineFunction(Function function) {
        functions.put(function.getName(), function);
    }

    public Function getFunction(String name) {
        Function function = functions.get(name);
        if (function != null) {
            return function;
        }
        
        // Check if it's a function stored as a variable (for arrow functions)
        Object obj = getVariable(name);
        if (obj instanceof Function) {
            return (Function) obj;
        }
        
        if (parent != null) {
            return parent.getFunction(name);
        }
        return null;
    }

    public void defineStruct(Struct struct) {
        structs.put(struct.getName(), struct);
    }

    public Struct getStruct(String name) {
        Struct struct = structs.get(name);
        if (struct != null) {
            return struct;
        }
        if (parent != null) {
            return parent.getStruct(name);
        }
        return null;
    }
}