/**
 * MicroScript — The programming language
 * Copyright (c) 2025-2026 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.HashMap;
import java.util.Map;
import com.magayaga.microscript.NativeIo; // import native IO bindings

public class Import {
    private static final Map<String, Module> modules = new HashMap<>();

    static {
        // Register built-in modules
        modules.put("math", new MathModule());
        modules.put("io", new IoModule());
        modules.put("http", new HttpModule());
    }

    public static void importModule(String name, Environment env) {
        Module module = modules.get(name);
        if (module != null) {
            module.register(env);
        } else {
            throw new RuntimeException("Module not found: " + name);
        }
    }

    // Module interface
    public interface Module {
        void register(Environment env);
    }

    // Example math module
    public static class MathModule implements Module {
        @Override
        public void register(Environment env) {
            env.setVariable("math::numbers::pi", NativeMath.PI());
            env.setVariable("math::numbers::e", NativeMath.E());
            env.setVariable("math::numbers::eulerNumber", NativeMath.E());
            env.setVariable("math::numbers::tau", NativeMath.TAU());
            env.setVariable("math::numbers::phi", NativeMath.PHI());
            env.setVariable("math::numbers::silverRatio", NativeMath.SILVERRATIO());
            env.setVariable("math::numbers::eulerConstant", NativeMath.EULER());
            env.setVariable("math::numbers::catalan", NativeMath.CATALAN());
            env.setVariable("math::numbers::apery", NativeMath.APERY());
            env.setVariable("math::numbers::feigenbaumDelta", NativeMath.FEIGENBAUMDELTA());
            env.setVariable("math::numbers::feigenbaumAlpha", NativeMath.FEIGENBAUMALPHA());
            env.setVariable("math::numbers::plastic", NativeMath.PLASTIC());
            env.setVariable("math::numbers::twinPrime", NativeMath.TWINPRIME());
            env.setVariable("math::sqrt", (Import.FunctionInterface) (args) -> NativeMath.sqrt(((Number) args[0]).doubleValue()));
            env.setVariable("math::square", (Import.FunctionInterface) (args) -> NativeMath.square(((Number) args[0]).doubleValue()));
            env.setVariable("math::cbrt", (Import.FunctionInterface) (args) -> NativeMath.cbrt(((Number) args[0]).doubleValue()));
            env.setVariable("math::cube", (Import.FunctionInterface) (args) -> NativeMath.cube(((Number) args[0]).doubleValue()));
            env.setVariable("math::abs", (Import.FunctionInterface) (args) -> NativeMath.abs(((Number) args[0]).doubleValue()));
            env.setVariable("math::log10", (Import.FunctionInterface) (args) -> NativeMath.log10(((Number) args[0]).doubleValue()));
            env.setVariable("math::log2", (Import.FunctionInterface) (args) -> NativeMath.log2(((Number) args[0]).doubleValue()));
            env.setVariable("math::log", (Import.FunctionInterface) (args) -> NativeMath.log(((Number) args[0]).doubleValue()));
            env.setVariable("math::sin", (Import.FunctionInterface) (args) -> NativeMath.sin(((Number) args[0]).doubleValue()));
            env.setVariable("math::cos", (Import.FunctionInterface) (args) -> NativeMath.cos(((Number) args[0]).doubleValue()));
            env.setVariable("math::tan", (Import.FunctionInterface) (args) -> NativeMath.tan(((Number) args[0]).doubleValue()));
            env.setVariable("math::asin", (Import.FunctionInterface) (args) -> NativeMath.asin(((Number) args[0]).doubleValue()));
            env.setVariable("math::acos", (Import.FunctionInterface) (args) -> NativeMath.acos(((Number) args[0]).doubleValue()));
            env.setVariable("math::atan", (Import.FunctionInterface) (args) -> NativeMath.atan(((Number) args[0]).doubleValue()));
            env.setVariable("math::atan2", (Import.FunctionInterface) (args) -> NativeMath.atan2(((Number) args[0]).doubleValue(), ((Number) args[1]).doubleValue()));
            env.setVariable("math::sinh", (Import.FunctionInterface) (args) -> NativeMath.sinh(((Number) args[0]).doubleValue()));
            env.setVariable("math::cosh", (Import.FunctionInterface) (args) -> NativeMath.cosh(((Number) args[0]).doubleValue()));
            env.setVariable("math::tanh", (Import.FunctionInterface) (args) -> NativeMath.tanh(((Number) args[0]).doubleValue()));
            env.setVariable("math::asinh", (Import.FunctionInterface) (args) -> NativeMath.asinh(((Number) args[0]).doubleValue()));
            env.setVariable("math::acosh", (Import.FunctionInterface) (args) -> NativeMath.acosh(((Number) args[0]).doubleValue()));
            env.setVariable("math::atanh", (Import.FunctionInterface) (args) -> NativeMath.atanh(((Number) args[0]).doubleValue()));
            // Add more math functions as needed
        }
    }

    // IO module
    public static class IoModule implements Module {
        @Override
        public void register(Environment env) {
            // print: prints string or ASCII code without newline
            env.setVariable("io::print", (Import.FunctionInterface) (args) -> {
                if (args[0] instanceof String) {
                    NativeIo.print((String) args[0]);
                } else if (args[0] instanceof Number) {
                    NativeIo.print(((Number) args[0]).intValue());
                }
                return null;
            });
            // println: prints string or ASCII code with newline
            env.setVariable("io::println", (Import.FunctionInterface) (args) -> {
                if (args[0] instanceof String) {
                    NativeIo.println((String) args[0]);
                } else if (args[0] instanceof Number) {
                    NativeIo.println(((Number) args[0]).intValue());
                }
                return null;
            });
        }
    }

    // HTTP module
    public static class HttpModule implements Module {
        @Override
        public void register(Environment env) {
            // Check if library is loaded
            env.setVariable("http::isLibraryLoaded", (Import.FunctionInterface) (args) -> {
                return NativeHttp.isLibraryLoaded();
            });
            
            // Server management
            env.setVariable("http::createServer", (Import.FunctionInterface) (args) -> {
                NativeHttp.checkLibrary();
                int port = ((Number) args[0]).intValue();
                return NativeHttp.createServer(port);
            });
            
            env.setVariable("http::stopServer", (Import.FunctionInterface) (args) -> {
                int serverHandle = ((Number) args[0]).intValue();
                NativeHttp.stopServer(serverHandle);
                return null;
            });
            
            env.setVariable("http::isRunning", (Import.FunctionInterface) (args) -> {
                int serverHandle = ((Number) args[0]).intValue();
                return NativeHttp.isRunning(serverHandle);
            });
            
            // Route management
            env.setVariable("http::addRoute", (Import.FunctionInterface) (args) -> {
                int serverHandle = ((Number) args[0]).intValue();
                String method = (String) args[1];
                String path = (String) args[2];
                String handlerName = (String) args[3];
                NativeHttp.addRoute(serverHandle, method, path, handlerName);
                return null;
            });
            
            env.setVariable("http::removeRoute", (Import.FunctionInterface) (args) -> {
                int serverHandle = ((Number) args[0]).intValue();
                String method = (String) args[1];
                String path = (String) args[2];
                NativeHttp.removeRoute(serverHandle, method, path);
                return null;
            });
            
            // Response utilities
            env.setVariable("http::setResponseHeader", (Import.FunctionInterface) (args) -> {
                int requestId = ((Number) args[0]).intValue();
                String name = (String) args[1];
                String value = (String) args[2];
                NativeHttp.setResponseHeader(requestId, name, value);
                return null;
            });
            
            env.setVariable("http::sendResponse", (Import.FunctionInterface) (args) -> {
                int requestId = ((Number) args[0]).intValue();
                int statusCode = ((Number) args[1]).intValue();
                String contentType = (String) args[2];
                String body = (String) args[3];
                NativeHttp.sendResponse(requestId, statusCode, contentType, body);
                return null;
            });
            
            env.setVariable("http::sendJsonResponse", (Import.FunctionInterface) (args) -> {
                int requestId = ((Number) args[0]).intValue();
                int statusCode = ((Number) args[1]).intValue();
                String jsonBody = (String) args[2];
                NativeHttp.sendJsonResponse(requestId, statusCode, jsonBody);
                return null;
            });
            
            env.setVariable("http::sendFileResponse", (Import.FunctionInterface) (args) -> {
                int requestId = ((Number) args[0]).intValue();
                String filePath = (String) args[1];
                NativeHttp.sendFileResponse(requestId, filePath);
                return null;
            });
            
            // Request information
            env.setVariable("http::getRequestPath", (Import.FunctionInterface) (args) -> {
                int requestId = ((Number) args[0]).intValue();
                return NativeHttp.getRequestPath(requestId);
            });
            
            env.setVariable("http::getRequestMethod", (Import.FunctionInterface) (args) -> {
                int requestId = ((Number) args[0]).intValue();
                return NativeHttp.getRequestMethod(requestId);
            });
            
            env.setVariable("http::getRequestHeader", (Import.FunctionInterface) (args) -> {
                int requestId = ((Number) args[0]).intValue();
                String headerName = (String) args[1];
                return NativeHttp.getRequestHeader(requestId, headerName);
            });
            
            env.setVariable("http::getRequestBody", (Import.FunctionInterface) (args) -> {
                int requestId = ((Number) args[0]).intValue();
                return NativeHttp.getRequestBody(requestId);
            });
            
            env.setVariable("http::getQueryParam", (Import.FunctionInterface) (args) -> {
                int requestId = ((Number) args[0]).intValue();
                String paramName = (String) args[1];
                return NativeHttp.getQueryParam(requestId, paramName);
            });
            
            // Middleware
            env.setVariable("http::useMiddleware", (Import.FunctionInterface) (args) -> {
                int serverHandle = ((Number) args[0]).intValue();
                String middlewareName = (String) args[1];
                NativeHttp.useMiddleware(serverHandle, middlewareName);
                return null;
            });
            
            // Utility functions
            env.setVariable("http::urlEncode", (Import.FunctionInterface) (args) -> {
                String input = (String) args[0];
                return NativeHttp.urlEncode(input);
            });
            
            env.setVariable("http::urlDecode", (Import.FunctionInterface) (args) -> {
                String input = (String) args[0];
                return NativeHttp.urlDecode(input);
            });
            
            env.setVariable("http::generateUuid", (Import.FunctionInterface) (args) -> {
                return NativeHttp.generateUuid();
            });
            
            // WebSocket support
            env.setVariable("http::createWebSocketEndpoint", (Import.FunctionInterface) (args) -> {
                int serverHandle = ((Number) args[0]).intValue();
                String path = (String) args[1];
                return NativeHttp.createWebSocketEndpoint(serverHandle, path);
            });
            
            env.setVariable("http::sendWebSocketMessage", (Import.FunctionInterface) (args) -> {
                int endpointHandle = ((Number) args[0]).intValue();
                String clientId = (String) args[1];
                String message = (String) args[2];
                NativeHttp.sendWebSocketMessage(endpointHandle, clientId, message);
                return null;
            });
            
            env.setVariable("http::broadcastWebSocketMessage", (Import.FunctionInterface) (args) -> {
                int endpointHandle = ((Number) args[0]).intValue();
                String message = (String) args[1];
                NativeHttp.broadcastWebSocketMessage(endpointHandle, message);
                return null;
            });
            
            env.setVariable("http::closeWebSocketConnection", (Import.FunctionInterface) (args) -> {
                int endpointHandle = ((Number) args[0]).intValue();
                String clientId = (String) args[1];
                NativeHttp.closeWebSocketConnection(endpointHandle, clientId);
                return null;
            });
        }
    }

    // Functional interface for native functions
    public interface FunctionInterface {
        Object call(Object[] args);
    }
}
