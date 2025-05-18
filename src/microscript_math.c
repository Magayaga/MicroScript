/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * It was originally written in C programming language.
 */
#include <jni.h>
#include <math.h>
#include <stdio.h>

// Square root
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_sqrt(JNIEnv *env, jclass cls, jdouble value) {
    return sqrt(value);
}

// Pi
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_PI(JNIEnv *env, jclass cls) {
    return M_PI;
}

// Euler's number
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_E(JNIEnv *env, jclass cls) {
    return M_E;
}

// Tau is equal to 2 * Pi
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_TAU(JNIEnv *env, jclass cls) {
    return 6.28318530717958647692;
}

// Golden ratio or Phi
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_PHI(JNIEnv *env, jclass cls) {
    return 1.61803398874989484820;
}

// Silver ratio
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_SILVERRATIO(JNIEnv *env, jclass cls) {
    return 2.41421356237309504880;
}

// Square
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_square
  (JNIEnv *env, jclass cls, jdouble value) {
    return value * value;
}

// Cube root
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_cbrt(JNIEnv *env, jclass cls, jdouble value) {
    return cbrt(value);
}

// Cube
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_cube
  (JNIEnv *env, jclass cls, jdouble value) {
    return value * value * value;
}

// Absolute value
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_abs(JNIEnv *env, jclass cls, jdouble value) {
    return fabs(value);
}

// Logarithm base 10
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_log10(JNIEnv *env, jclass cls, jdouble value) {
    return log10(value);
}

// Logarithm base e
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_log(JNIEnv *env, jclass cls, jdouble value) {
    return log(value);
}

// Logarithm base 2
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_log2(JNIEnv *env, jclass cls, jdouble value) {
    return log2(value);
}
