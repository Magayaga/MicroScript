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

// Euler's constant
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_EULER(JNIEnv *env, jclass cls) {
    return 0.57721566490153286060;
}

// Catalan's constant
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_CATALAN(JNIEnv *env, jclass cls) {
    return 0.91596559417721901505;
}

// Apéry's constant
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_APERY(JNIEnv *env, jclass cls) {
    return 1.20205690315959428540;
}

// Feigenbaum constant delta
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_FEIGENBAUMDELTA(JNIEnv *env, jclass cls) {
    return 4.66920160910299067185;
}

// Feigenbaum constant alpha
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_FEIGENBAUMALPHA(JNIEnv *env, jclass cls) {
    return 2.50290787509589282228;
}

// Plastic constant
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_PLASTIC(JNIEnv *env, jclass cls) {
    return 1.32471795724474602596;
}

// Twin prime constant
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_TWINPRIME(JNIEnv *env, jclass cls) {
    return 0.66016181584686957392;
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
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_cube(JNIEnv *env, jclass cls, jdouble value) {
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

// Sine
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_sin(JNIEnv *env, jclass cls, jdouble value) {
    return sin(value);
}

// Cosine
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_cos(JNIEnv *env, jclass cls, jdouble value) {
    return cos(value);
}

// Tangent
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_tan(JNIEnv *env, jclass cls, jdouble value) {
    return tan(value);
}

// Inverse sine
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_asin(JNIEnv *env, jclass cls, jdouble value) {
    return asin(value);
}

// Inverse cosine
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_acos(JNIEnv *env, jclass cls, jdouble value) {
    return acos(value);
}

// Inverse tangent
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_atan(JNIEnv *env, jclass cls, jdouble value) {
    return atan(value);
}

// Inverse tangent with two arguments
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_atan2(JNIEnv *env, jclass cls, jdouble y, jdouble x) {
    return atan2(y, x);
}

// Hyperbolic sine
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_sinh(JNIEnv *env, jclass cls, jdouble value) {
    return sinh(value);
}

// Hyperbolic cosine
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_cosh(JNIEnv *env, jclass cls, jdouble value) {
    return cosh(value);
}

// Hyperbolic tangent
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_tanh(JNIEnv *env, jclass cls, jdouble value) {
    return tanh(value);
}

// Hyperbolic inverse sine
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_asinh(JNIEnv *env, jclass cls, jdouble value) {
    return asinh(value);
}

// Hyperbolic inverse cosine
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_acosh(JNIEnv *env, jclass cls, jdouble value) {
    return acosh(value);
}

// Hyperbolic inverse tangent
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_atanh(JNIEnv *env, jclass cls, jdouble value) {
    return atanh(value);
}

// Exponential function
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_exp(JNIEnv *env, jclass cls, jdouble value) {
    return exp(value);
}

// Exponential function base 2
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_exp2(JNIEnv *env, jclass cls, jdouble value) {
    return exp2(value);
}

// Exponential function base 10
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_exp10(JNIEnv *env, jclass cls, jdouble value) {
    return pow(10, value);
}

// Power function
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_pow(JNIEnv *env, jclass cls, jdouble base, jdouble exponent) {
    return pow(base, exponent);
}
// Rounding functions
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_round(JNIEnv *env, jclass cls, jdouble value) {
    return round(value);
}

// Ceiling function
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_ceil(JNIEnv *env, jclass cls, jdouble value) {
    return ceil(value);
}

// Floor function
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_floor(JNIEnv *env, jclass cls, jdouble value) {
    return floor(value);
}

// Minimum function
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_min(JNIEnv *env, jclass cls, jdouble a, jdouble b) {
    return fmin(a, b);
}

// Maximum function
JNIEXPORT jdouble JNICALL Java_com_magayaga_microscript_NativeMath_max(JNIEnv *env, jclass cls, jdouble a, jdouble b) {
    return fmax(a, b);
}
