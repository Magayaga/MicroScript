/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

public class NativeIo {
    static { System.loadLibrary("io"); }

    public static native void print(String message);
    public static native void print(int code);
    public static native void println(String message);
    public static native void println(int code);
}
