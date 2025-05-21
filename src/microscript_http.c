/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * JNI bridge for the HTTP server functionality
 */
#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include "httpserver.h" // Go functions exported via CGO

// JNI function naming convention: Java_packagename_classname_methodname
// For com.magayaga.microscript.NativeHttp

JNIEXPORT jint JNICALL Java_com_magayaga_microscript_NativeHttp_createServer
  (JNIEnv *env, jclass cls, jint port) {
    return createServer((int)port);
}

JNIEXPORT void JNICALL Java_com_magayaga_microscript_NativeHttp_stopServer
  (JNIEnv *env, jclass cls, jint serverHandle) {
    stopServer((int)serverHandle);
}

JNIEXPORT jboolean JNICALL Java_com_magayaga_microscript_NativeHttp_isRunning
  (JNIEnv *env, jclass cls, jint serverHandle) {
    return isRunning((int)serverHandle) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_com_magayaga_microscript_NativeHttp_addRoute
  (JNIEnv *env, jclass cls, jint serverHandle, jstring method, jstring path, jstring handlerName) {
    const char *methodStr = (*env)->GetStringUTFChars(env, method, NULL);
    const char *pathStr = (*env)->GetStringUTFChars(env, path, NULL);
    const char *handlerNameStr = (*env)->GetStringUTFChars(env, handlerName, NULL);
    
    addRoute((int)serverHandle, (char*)methodStr, (char*)pathStr, (char*)handlerNameStr);
    
    (*env)->ReleaseStringUTFChars(env, method, methodStr);
    (*env)->ReleaseStringUTFChars(env, path, pathStr);
    (*env)->ReleaseStringUTFChars(env, handlerName, handlerNameStr);
}

JNIEXPORT void JNICALL Java_com_magayaga_microscript_NativeHttp_removeRoute
  (JNIEnv *env, jclass cls, jint serverHandle, jstring method, jstring path) {
    const char *methodStr = (*env)->GetStringUTFChars(env, method, NULL);
    const char *pathStr = (*env)->GetStringUTFChars(env, path, NULL);
    
    removeRoute((int)serverHandle, (char*)methodStr, (char*)pathStr);
    
    (*env)->ReleaseStringUTFChars(env, method, methodStr);
    (*env)->ReleaseStringUTFChars(env, path, pathStr);
}

JNIEXPORT void JNICALL Java_com_magayaga_microscript_NativeHttp_setResponseHeader
  (JNIEnv *env, jclass cls, jint requestId, jstring name, jstring value) {
    const char *nameStr = (*env)->GetStringUTFChars(env, name, NULL);
    const char *valueStr = (*env)->GetStringUTFChars(env, value, NULL);
    
    setResponseHeader((int)requestId, (char*)nameStr, (char*)valueStr);
    
    (*env)->ReleaseStringUTFChars(env, name, nameStr);
    (*env)->ReleaseStringUTFChars(env, value, valueStr);
}

JNIEXPORT void JNICALL Java_com_magayaga_microscript_NativeHttp_sendResponse
  (JNIEnv *env, jclass cls, jint requestId, jint statusCode, jstring contentType, jstring body) {
    const char *contentTypeStr = (*env)->GetStringUTFChars(env, contentType, NULL);
    const char *bodyStr = (*env)->GetStringUTFChars(env, body, NULL);
    
    sendResponse((int)requestId, (int)statusCode, (char*)contentTypeStr, (char*)bodyStr);
    
    (*env)->ReleaseStringUTFChars(env, contentType, contentTypeStr);
    (*env)->ReleaseStringUTFChars(env, body, bodyStr);
}

JNIEXPORT void JNICALL Java_com_magayaga_microscript_NativeHttp_sendJsonResponse
  (JNIEnv *env, jclass cls, jint requestId, jint statusCode, jstring jsonBody) {
    const char *jsonBodyStr = (*env)->GetStringUTFChars(env, jsonBody, NULL);
    
    sendJsonResponse((int)requestId, (int)statusCode, (char*)jsonBodyStr);
    
    (*env)->ReleaseStringUTFChars(env, jsonBody, jsonBodyStr);
}

JNIEXPORT void JNICALL Java_com_magayaga_microscript_NativeHttp_sendFileResponse
  (JNIEnv *env, jclass cls, jint requestId, jstring filePath) {
    const char *filePathStr = (*env)->GetStringUTFChars(env, filePath, NULL);
    
    sendFileResponse((int)requestId, (char*)filePathStr);
    
    (*env)->ReleaseStringUTFChars(env, filePath, filePathStr);
}

JNIEXPORT jstring JNICALL Java_com_magayaga_microscript_NativeHttp_getRequestPath
  (JNIEnv *env, jclass cls, jint requestId) {
    char *path = getRequestPath((int)requestId);
    jstring result = (*env)->NewStringUTF(env, path);
    free(path);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_magayaga_microscript_NativeHttp_getRequestMethod
  (JNIEnv *env, jclass cls, jint requestId) {
    char *method = getRequestMethod((int)requestId);
    jstring result = (*env)->NewStringUTF(env, method);
    free(method);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_magayaga_microscript_NativeHttp_getRequestHeader
  (JNIEnv *env, jclass cls, jint requestId, jstring headerName) {
    const char *headerNameStr = (*env)->GetStringUTFChars(env, headerName, NULL);
    
    char *headerValue = getRequestHeader((int)requestId, (char*)headerNameStr);
    jstring result = (*env)->NewStringUTF(env, headerValue);
    
    (*env)->ReleaseStringUTFChars(env, headerName, headerNameStr);
    free(headerValue);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_magayaga_microscript_NativeHttp_getRequestBody
  (JNIEnv *env, jclass cls, jint requestId) {
    char *body = getRequestBody((int)requestId);
    jstring result = (*env)->NewStringUTF(env, body);
    free(body);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_magayaga_microscript_NativeHttp_getQueryParam
  (JNIEnv *env, jclass cls, jint requestId, jstring paramName) {
    const char *paramNameStr = (*env)->GetStringUTFChars(env, paramName, NULL);
    
    char *paramValue = getQueryParam((int)requestId, (char*)paramNameStr);
    jstring result = (*env)->NewStringUTF(env, paramValue);
    
    (*env)->ReleaseStringUTFChars(env, paramName, paramNameStr);
    free(paramValue);
    return result;
}

JNIEXPORT void JNICALL Java_com_magayaga_microscript_NativeHttp_useMiddleware
  (JNIEnv *env, jclass cls, jint serverHandle, jstring middlewareName) {
    const char *middlewareNameStr = (*env)->GetStringUTFChars(env, middlewareName, NULL);
    
    useMiddleware((int)serverHandle, (char*)middlewareNameStr);
    
    (*env)->ReleaseStringUTFChars(env, middlewareName, middlewareNameStr);
}

JNIEXPORT jstring JNICALL Java_com_magayaga_microscript_NativeHttp_urlEncode
  (JNIEnv *env, jclass cls, jstring input) {
    const char *inputStr = (*env)->GetStringUTFChars(env, input, NULL);
    
    char *encoded = urlEncode((char*)inputStr);
    jstring result = (*env)->NewStringUTF(env, encoded);
    
    (*env)->ReleaseStringUTFChars(env, input, inputStr);
    free(encoded);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_magayaga_microscript_NativeHttp_urlDecode
  (JNIEnv *env, jclass cls, jstring input) {
    const char *inputStr = (*env)->GetStringUTFChars(env, input, NULL);
    
    char *decoded = urlDecode((char*)inputStr);
    jstring result = (*env)->NewStringUTF(env, decoded);
    
    (*env)->ReleaseStringUTFChars(env, input, inputStr);
    free(decoded);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_magayaga_microscript_NativeHttp_generateUuid
  (JNIEnv *env, jclass cls) {
    char *uuid = generateUuid();
    jstring result = (*env)->NewStringUTF(env, uuid);
    free(uuid);
    return result;
}

JNIEXPORT jint JNICALL Java_com_magayaga_microscript_NativeHttp_createWebSocketEndpoint
  (JNIEnv *env, jclass cls, jint serverHandle, jstring path) {
    const char *pathStr = (*env)->GetStringUTFChars(env, path, NULL);
    
    int result = createWebSocketEndpoint((int)serverHandle, (char*)pathStr);
    
    (*env)->ReleaseStringUTFChars(env, path, pathStr);
    return result;
}

JNIEXPORT void JNICALL Java_com_magayaga_microscript_NativeHttp_sendWebSocketMessage
  (JNIEnv *env, jclass cls, jint endpointHandle, jstring clientId, jstring message) {
    const char *clientIdStr = (*env)->GetStringUTFChars(env, clientId, NULL);
    const char *messageStr = (*env)->GetStringUTFChars(env, message, NULL);
    
    sendWebSocketMessage((int)endpointHandle, (char*)clientIdStr, (char*)messageStr);
    
    (*env)->ReleaseStringUTFChars(env, clientId, clientIdStr);
    (*env)->ReleaseStringUTFChars(env, message, messageStr);
}

JNIEXPORT void JNICALL Java_com_magayaga_microscript_NativeHttp_broadcastWebSocketMessage
  (JNIEnv *env, jclass cls, jint endpointHandle, jstring message) {
    const char *messageStr = (*env)->GetStringUTFChars(env, message, NULL);
    
    broadcastWebSocketMessage((int)endpointHandle, (char*)messageStr);
    
    (*env)->ReleaseStringUTFChars(env, message, messageStr);
}

JNIEXPORT void JNICALL Java_com_magayaga_microscript_NativeHttp_closeWebSocketConnection
  (JNIEnv *env, jclass cls, jint endpointHandle, jstring clientId) {
    const char *clientIdStr = (*env)->GetStringUTFChars(env, clientId, NULL);
    
    closeWebSocketConnection((int)endpointHandle, (char*)clientIdStr);
    
    (*env)->ReleaseStringUTFChars(env, clientId, clientIdStr);
}
