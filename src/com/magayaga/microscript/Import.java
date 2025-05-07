/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.HashMap;
import java.util.Map;

public class Import {
    private static final Map<String, Module> modules = new HashMap<>();

    static {
        // Register built-in modules
        modules.put("math", new MathModule());
    }

    public static void importModule(String name, Environment env) {
        Module module = modules.get(name);
        if (module != null) {
            module.register(env);
        } else {
            throw new RuntimeException("Module not found: " + name);
        }
    }

    // Module interface
    public interface Module {
        void register(Environment env);
    }

    // Example math module
    public static class MathModule implements Module {
        @Override
        public void register(Environment env) {
            env.setVariable("math::PI", NativeMath.PI());
            env.setVariable("math::E", NativeMath.E());
            env.setVariable("math::TAU", NativeMath.TAU());
            env.setVariable("math::PHI", NativeMath.PHI());
            env.setVariable("math::sqrt", (Import.FunctionInterface) (args) -> NativeMath.sqrt(((Number)args[0]).doubleValue()));
            env.setVariable("math::square", (Import.FunctionInterface) (args) -> NativeMath.square(((Number)args[0]).doubleValue()));
            env.setVariable("math::cbrt", (Import.FunctionInterface) (args) -> NativeMath.cbrt(((Number)args[0]).doubleValue()));
            env.setVariable("math::cube", (Import.FunctionInterface) (args) -> NativeMath.cube(((Number)args[0]).doubleValue()));
            // Add more math functions as needed
        }
    }

    // Functional interface for native functions
    public interface FunctionInterface {
        Object call(Object[] args);
    }
}