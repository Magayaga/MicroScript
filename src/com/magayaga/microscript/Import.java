/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.HashMap;
import java.util.Map;
import com.magayaga.microscript.NativeIo; // import native IO bindings

public class Import {
    private static final Map<String, Module> modules = new HashMap<>();

    static {
        // Register built-in modules
        modules.put("math", new MathModule());
        modules.put("io", new IoModule());
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
            env.setVariable("math::SILVERRATIO", NativeMath.SILVERRATIO());
            env.setVariable("math::sqrt", (Import.FunctionInterface) (args) -> NativeMath.sqrt(((Number) args[0]).doubleValue()));
            env.setVariable("math::square", (Import.FunctionInterface) (args) -> NativeMath.square(((Number) args[0]).doubleValue()));
            env.setVariable("math::cbrt", (Import.FunctionInterface) (args) -> NativeMath.cbrt(((Number) args[0]).doubleValue()));
            env.setVariable("math::cube", (Import.FunctionInterface) (args) -> NativeMath.cube(((Number) args[0]).doubleValue()));
            env.setVariable("math::abs", (Import.FunctionInterface) (args) -> NativeMath.abs(((Number) args[0]).doubleValue()));
            env.setVariable("math::log10", (Import.FunctionInterface) (args) -> NativeMath.log10(((Number) args[0]).doubleValue()));
            env.setVariable("math::log2", (Import.FunctionInterface) (args) -> NativeMath.log2(((Number) args[0]).doubleValue()));
            env.setVariable("math::log", (Import.FunctionInterface) (args) -> NativeMath.log(((Number) args[0]).doubleValue()));
            // Add more math functions as needed
        }
    }

    // IO module
    public static class IoModule implements Module {
        @Override
        public void register(Environment env) {
            // print: prints string or ASCII code without newline
            env.setVariable("io::print", (Import.FunctionInterface) (args) -> {
                if (args[0] instanceof String) {
                    NativeIo.print((String) args[0]);
                } else if (args[0] instanceof Number) {
                    NativeIo.print(((Number) args[0]).intValue());
                }
                return null;
            });
            // println: prints string or ASCII code with newline
            env.setVariable("io::println", (Import.FunctionInterface) (args) -> {
                if (args[0] instanceof String) {
                    NativeIo.println((String) args[0]);
                } else if (args[0] instanceof Number) {
                    NativeIo.println(((Number) args[0]).intValue());
                }
                return null;
            });
        }
    }

    // Functional interface for native functions
    public interface FunctionInterface {
        Object call(Object[] args);
    }
}
