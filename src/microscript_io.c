/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * It was originally written in C programming language.
 */
#include <jni.h>
#include <stdio.h>


JNIEXPORT void JNICALL Java_com_magayaga_microscript_NativeIo_print__Ljava_lang_String_2
  (JNIEnv *env, jclass cls, jstring message)
{
    const char *msg = (*env)->GetStringUTFChars(env, message, NULL);
    if (msg == NULL) return; // JVM threw OutOfMemoryError

    printf("%s", msg);
    fflush(stdout);
    (*env)->ReleaseStringUTFChars(env, message, msg);
}

JNIEXPORT void JNICALL Java_com_magayaga_microscript_NativeIo_println__Ljava_lang_String_2
  (JNIEnv *env, jclass cls, jstring message)
{
    const char *msg = (*env)->GetStringUTFChars(env, message, NULL);
    if (msg == NULL) return;

    printf("%s\n", msg);
    fflush(stdout);
    (*env)->ReleaseStringUTFChars(env, message, msg);
}

JNIEXPORT void JNICALL Java_com_magayaga_microscript_NativeIo_print__I
  (JNIEnv *env, jclass cls, jint code)
{
    unsigned char c = (unsigned char) code;
    putchar(c);
    fflush(stdout);
}

JNIEXPORT void JNICALL Java_com_magayaga_microscript_NativeIo_println__I
  (JNIEnv *env, jclass cls, jint code)
{
    unsigned char c = (unsigned char) code;
    putchar(c);
    putchar('\n');
    fflush(stdout);
}
