/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

public class NativeHttp {
    static {
        System.loadLibrary("httpserver"); // Loads httpserver.dll or libhttpserver.so
    }
    
    // HTTP Server core functions
    public static native int createServer(int port);
    public static native void stopServer(int serverHandle);
    public static native boolean isRunning(int serverHandle);
    
    // Route handling
    public static native void addRoute(int serverHandle, String method, String path, String handlerName);
    public static native void removeRoute(int serverHandle, String method, String path);
    
    // Response utilities
    public static native void setResponseHeader(int requestId, String name, String value);
    public static native void sendResponse(int requestId, int statusCode, String contentType, String body);
    public static native void sendJsonResponse(int requestId, int statusCode, String jsonBody);
    public static native void sendFileResponse(int requestId, String filePath);
    
    // Request information
    public static native String getRequestPath(int requestId);
    public static native String getRequestMethod(int requestId);
    public static native String getRequestHeader(int requestId, String headerName);
    public static native String getRequestBody(int requestId);
    public static native String getQueryParam(int requestId, String paramName);
    
    // Middleware
    public static native void useMiddleware(int serverHandle, String middlewareName);
    
    // Utility functions
    public static native String urlEncode(String input);
    public static native String urlDecode(String input);
    public static native String generateUuid();
    
    // WebSocket support
    public static native int createWebSocketEndpoint(int serverHandle, String path);
    public static native void sendWebSocketMessage(int endpointHandle, String clientId, String message);
    public static native void broadcastWebSocketMessage(int endpointHandle, String message);
    public static native void closeWebSocketConnection(int endpointHandle, String clientId);
}