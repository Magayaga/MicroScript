/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2026 Cyril John Magayaga
 *
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an interface contract in MicroScript.
 */
public class InterfaceContract {
    private final String name;
    private final Map<String, String> properties; // property name -> type
    private final Map<String, MethodSignature> methods; // method name -> signature

    public InterfaceContract(String name, Map<String, String> properties, Map<String, MethodSignature> methods) {
        this.name = name;
        this.properties = new LinkedHashMap<>(properties);
        this.methods = new LinkedHashMap<>(methods);
    }

    public static InterfaceContract parseInterfaceDefinition(List<String> lines, int startIndex) {
        String firstLine = lines.get(startIndex).trim();
        Pattern headerPattern = Pattern.compile("interface\\s+([\\p{L}_][\\p{L}\\p{N}_]*)\\s*(\\{)?");
        Matcher headerMatcher = headerPattern.matcher(firstLine);

        if (!headerMatcher.matches()) {
            throw new RuntimeException("Invalid interface declaration syntax: " + firstLine);
        }

        String interfaceName = headerMatcher.group(1);
        Map<String, String> properties = new LinkedHashMap<>();
        Map<String, MethodSignature> methods = new LinkedHashMap<>();

        int bodyStartIndex = startIndex + 1;
        if (headerMatcher.group(2) == null) {
            boolean foundOpeningBrace = false;
            while (bodyStartIndex < lines.size()) {
                String line = lines.get(bodyStartIndex).trim();
                if (line.isEmpty() || line.startsWith("//")) {
                    bodyStartIndex++;
                    continue;
                }

                if ("{".equals(line)) {
                    foundOpeningBrace = true;
                    bodyStartIndex++;
                    break;
                }

                throw new RuntimeException("Expected '{' after interface declaration: " + firstLine);
            }

            if (!foundOpeningBrace) {
                throw new RuntimeException("Missing opening brace for interface: " + interfaceName);
            }
        }

        for (int i = bodyStartIndex; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            if (line.equals("}")) {
                break;
            }

            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }

            if (line.startsWith("var ") || line.startsWith("bool ")) {
                parseProperty(properties, line);
                continue;
            }

            if (line.startsWith("function ")) {
                MethodSignature signature = MethodSignature.parse(line);
                methods.put(signature.getName(), signature);
                continue;
            }

            throw new RuntimeException("Invalid syntax in interface definition: " + line);
        }

        return new InterfaceContract(interfaceName, properties, methods);
    }

    private static void parseProperty(Map<String, String> properties, String line) {
        String declaration = line.startsWith("bool ")
                ? line.substring(5).trim()
                : line.substring(4).trim();
        if (declaration.endsWith(";")) {
            declaration = declaration.substring(0, declaration.length() - 1).trim();
        }

        String[] parts = declaration.split(":");
        if (parts.length != 2) {
            throw new RuntimeException("Invalid interface property declaration: " + line);
        }

        properties.put(parts[0].trim(), parts[1].trim());
    }

    public boolean isSatisfiedBy(Struct structInstance) {
        if (structInstance == null || structInstance.isDefinition()) {
            return false;
        }

        Map<String, String> actualFields = structInstance.getFields();
        for (Map.Entry<String, String> requiredProperty : properties.entrySet()) {
            String requiredName = requiredProperty.getKey();
            String requiredType = requiredProperty.getValue();
            String actualType = actualFields.get(requiredName);

            if (actualType == null || !requiredType.equals(actualType)) {
                return false;
            }
        }

        // Methods are parsed and stored for future class support.
        return true;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getProperties() {
        return new LinkedHashMap<>(properties);
    }

    public Map<String, MethodSignature> getMethods() {
        return new LinkedHashMap<>(methods);
    }

    @Override
    public String toString() {
        return "interface " + name + " { properties=" + properties.keySet() + ", methods=" + methods.keySet() + " }";
    }

    public static class MethodSignature {
        private final String name;
        private final List<Parameter> parameters;
        private final String returnType;

        public MethodSignature(String name, List<Parameter> parameters, String returnType) {
            this.name = name;
            this.parameters = new ArrayList<>(parameters);
            this.returnType = returnType;
        }

        public static MethodSignature parse(String line) {
            Pattern methodPattern = Pattern.compile("function\\s+([\\p{L}_][\\p{L}\\p{N}_]*)\\s*\\(([^)]*)\\)\\s*(?:->\\s*([\\p{L}_][\\p{L}\\p{N}_]*(?:::[\\p{L}_][\\p{L}\\p{N}_]*)*))?\\s*;?");
            Matcher matcher = methodPattern.matcher(line);

            if (!matcher.matches()) {
                throw new RuntimeException("Invalid interface method declaration: " + line);
            }

            String methodName = matcher.group(1);
            String params = matcher.group(2).trim();
            String returnType = matcher.group(3) != null ? matcher.group(3).trim() : "void";

            List<Parameter> parsedParameters = new ArrayList<>();
            if (!params.isEmpty()) {
                String[] paramPairs = params.split(",");
                for (String paramPair : paramPairs) {
                    String[] parts = paramPair.trim().split(":");
                    if (parts.length != 2) {
                        throw new RuntimeException("Invalid interface method parameter declaration: " + paramPair);
                    }
                    parsedParameters.add(new Parameter(parts[0].trim(), parts[1].trim()));
                }
            }

            return new MethodSignature(methodName, parsedParameters, returnType);
        }

        public String getName() {
            return name;
        }

        public List<Parameter> getParameters() {
            return new ArrayList<>(parameters);
        }

        public String getReturnType() {
            return returnType;
        }
    }
}
