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
    public static native double SILVERRATIO();
    public static native double EULER();
    public static native double CATALAN();
    public static native double APERY();
    public static native double FEIGENBAUMDELTA();
    public static native double FEIGENBAUMALPHA();
    public static native double PLASTIC();
    public static native double TWINPRIME();
    public static native double square(double value);
    public static native double cube(double value);
    public static native double abs(double value);
    public static native double log10(double value);
    public static native double log2(double value);
    public static native double log(double value);
    public static native double sin(double value);
    public static native double cos(double value);
    public static native double tan(double value);
    public static native double asin(double value);
    public static native double acos(double value);
    public static native double atan(double value);
    public static native double atan2(double y, double x);
    public static native double sinh(double value);
    public static native double cosh(double value);
    public static native double tanh(double value);
    public static native double asinh(double value);
    public static native double acosh(double value);
    public static native double atanh(double value);
}