/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

public class NativeMath {
    static {
        System.loadLibrary("math"); // Loads math.dll or math.so
    }
    public static native double sqrt(double value);
    public static native double PI();
}