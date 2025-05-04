/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * It was originally written in C programming language.
 */
#include <jni.h>
#include <math.h>

JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_sqrt(JNIEnv *env, jclass cls, jdouble value) {
    return sqrt(value);
}

JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_PI(JNIEnv *env, jclass cls) {
    return M_PI;
}