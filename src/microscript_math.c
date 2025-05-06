/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * It was originally written in C programming language.
 */
#include <jni.h>
#include <math.h>
#include <stdio.h>

JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_sqrt(JNIEnv *env, jclass cls, jdouble value) {
    return sqrt(value);
}

JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_PI(JNIEnv *env, jclass cls) {
    return M_PI;
}

JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_E(JNIEnv *env, jclass cls) {
    return M_E;
}

JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_TAU(JNIEnv *env, jclass cls) {
    return 6.28318530717958647692;
}

JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_PHI(JNIEnv *env, jclass cls) {
    return 1.61803398874989484820;
}