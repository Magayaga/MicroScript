/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;  // Add this import

public class Class {
    private final String name;
    private final Map<String, Function> methods;
    private final Map<String, Property> properties;
    private final Environment classEnvironment;

    public Class(String name) {
        this.name = name;
        this.methods = new HashMap<>();
        this.properties = new HashMap<>();
        this.classEnvironment = new Environment();
    }

    public void addMethod(Function method) {
        methods.put(method.getName(), method);
    }

    public void addProperty(Property property) {
        properties.put(property.getName(), property);
        // Initialize property in class environment
        classEnvironment.setVariable(property.getName(), property.getDefaultValue());
    }

    public Function getMethod(String methodName) {
        return methods.get(methodName);
    }

    public Object getProperty(String propertyName) {
        return classEnvironment.getVariable(propertyName);
    }

    public void setProperty(String propertyName, Object value) {
        if (properties.containsKey(propertyName)) {
            Property prop = properties.get(propertyName);
            if (prop.validateType(value)) {
                classEnvironment.setVariable(propertyName, value);
            } else {
                throw new RuntimeException("Type mismatch for property: " + propertyName);
            }
        } else {
            throw new RuntimeException("Property not found: " + propertyName);
        }
    }

    public String getName() {
        return name;
    }

    public static class Property {
        private final String name;
        private final String type;
        private final Object defaultValue;

        public Property(String name, String type, Object defaultValue) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public boolean validateType(Object value) {
            if (value == null) return true;
            
            switch (type) {
                case "String":
                    return value instanceof String;
                case "Int32":
                case "Int64":
                    return value instanceof Integer;
                case "Float32":
                    return value instanceof Float;
                case "Float64":
                    return value instanceof Double;
                case "Char":
                    return value instanceof Character;
                case "Bool":
                    return value instanceof Boolean;
                default:
                    // For custom class types
                    return true;
            }
        }
    }

    public static class Instance {
        private final Class classDefinition;
        private final Environment instanceEnvironment;

        public Instance(Class classDefinition) {
            this.classDefinition = classDefinition;
            this.instanceEnvironment = new Environment(classDefinition.classEnvironment);
        }

        public Object invokeMethod(String methodName, Object[] args) {
            Function method = classDefinition.getMethod(methodName);
            if (method == null) {
                throw new RuntimeException("Method not found: " + methodName);
            }

            // Convert arguments to proper format
            String[] stringArgs = Arrays.stream(args)
                .map(arg -> {
                    if (arg instanceof Double) {
                        double d = (Double) arg;
                        return d == Math.floor(d) ? String.format("%.0f", d) : arg.toString();
                    }
                    return arg.toString();
                })
                .toArray(String[]::new);

            // Execute the method with proper environment
            Executor executor = new Executor(instanceEnvironment);
            return executor.executeFunction(methodName, stringArgs);
        }

        public Object getProperty(String propertyName) {
            return instanceEnvironment.getVariable(propertyName);
        }

        public void setProperty(String propertyName, Object value) {
            classDefinition.setProperty(propertyName, value);
        }
    }
}