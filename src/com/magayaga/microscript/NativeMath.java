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
    public static native double cbrt(double value);
    public static native double PI();
    public static native double E();
    public static native double TAU();
    public static native double PHI();
    public static native double square(double value);
    public static native double cube(double value);
    public static native double abs(double value);
    public static native double log10(double value);
    public static native double log2(double value);
    public static native double log(double value);
}