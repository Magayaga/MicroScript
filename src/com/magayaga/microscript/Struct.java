/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2026 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Represents a struct definition and instance in MicroScript
 */
public class Struct {
    private final String name;
    private final Map<String, String> fields; // field name -> type
    private final Map<String, Object> values; // field name -> value (for instances)
    private final boolean isDefinition; // true for struct definitions, false for instances
    
    /**
     * Constructor for struct definition
     */
    public Struct(String name, Map<String, String> fields) {
        this.name = name;
        this.fields = new LinkedHashMap<>(fields);
        this.values = null;
        this.isDefinition = true;
    }
    
    /**
     * Constructor for struct instance
     */
    public Struct(String name, Map<String, String> fields, Map<String, Object> values) {
        this.name = name;
        this.fields = new LinkedHashMap<>(fields);
        this.values = new LinkedHashMap<>(values);
        this.isDefinition = false;
        
        // Validate that all required fields are provided
        for (String fieldName : fields.keySet()) {
            if (!values.containsKey(fieldName)) {
                throw new RuntimeException("Missing field '" + fieldName + "' in struct " + name + " initialization");
            }
        }
        
        // Validate field types
        validateFieldTypes();
    }
    
    /**
     * Create an instance of this struct with the given values
     */
    public Struct createInstance(List<Object> initValues) {
        if (!isDefinition) {
            throw new RuntimeException("Cannot create instance from struct instance");
        }
        
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        if (initValues.size() != fieldNames.size()) {
            throw new RuntimeException("Struct " + name + " expects " + fieldNames.size() + 
                                     " values, but got " + initValues.size());
        }
        
        Map<String, Object> instanceValues = new LinkedHashMap<>();
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            Object value = initValues.get(i);
            instanceValues.put(fieldName, value);
        }
        
        return new Struct(name, fields, instanceValues);
    }
    
    /**
     * Get the value of a field
     */
    public Object getField(String fieldName) {
        if (isDefinition) {
            throw new RuntimeException("Cannot access field on struct definition");
        }
        
        if (!fields.containsKey(fieldName)) {
            throw new RuntimeException("Field '" + fieldName + "' does not exist in struct " + name);
        }
        
        return values.get(fieldName);
    }
    
    /**
     * Set the value of a field
     */
    public void setField(String fieldName, Object value) {
        if (isDefinition) {
            throw new RuntimeException("Cannot set field on struct definition");
        }
        
        if (!fields.containsKey(fieldName)) {
            throw new RuntimeException("Field '" + fieldName + "' does not exist in struct " + name);
        }
        
        String expectedType = fields.get(fieldName);
        if (!validateType(value, expectedType)) {
            throw new RuntimeException("Type error: Field '" + fieldName + "' expects " + 
                                     expectedType + " but got " + getTypeName(value));
        }
        
        values.put(fieldName, value);
    }
    
    /**
     * Validate that all field values match their declared types
     */
    private void validateFieldTypes() {
        for (Map.Entry<String, String> field : fields.entrySet()) {
            String fieldName = field.getKey();
            String expectedType = field.getValue();
            Object value = values.get(fieldName);
            
            if (!validateType(value, expectedType)) {
                throw new RuntimeException("Type error: Field '" + fieldName + "' expects " + 
                                         expectedType + " but got " + getTypeName(value));
            }
        }
    }
    
    /**
     * Validate if a value matches the expected type
     */
    private boolean validateType(Object value, String expectedType) {
        switch (expectedType) {
            case "String":
                return value instanceof String;
            case "Int32":
                return value instanceof Integer;
            case "Int64":
                return value instanceof Long || value instanceof Integer; // Allow integer to long conversion
            case "Float32":
                return value instanceof Float;
            case "Float64":
                return value instanceof Double || value instanceof Integer || value instanceof Float; // Allow numeric conversions
            case "Char":
                return value instanceof Character;
            case "bool":
                return value instanceof Boolean;
            default:
                // Could be another struct type - for now, return true
                return true;
        }
    }
    
    /**
     * Get the type name of a value
     */
    private String getTypeName(Object value) {
        if (value instanceof String) return "String";
        if (value instanceof Integer) return "Int32";
        if (value instanceof Long) return "Int64";
        if (value instanceof Float) return "Float32";
        if (value instanceof Double) return "Float64";
        if (value instanceof Character) return "Char";
        if (value instanceof Boolean) return "bool";
        if (value instanceof Struct) return ((Struct) value).getName();
        return value.getClass().getSimpleName();
    }
    
    /**
     * Parse a struct definition from lines of code
     */
    public static Struct parseStructDefinition(List<String> lines, int startIndex) {
        String firstLine = lines.get(startIndex).trim();
        
        // Extract struct name from "struct StructName {" or "struct StructName"
        String structName;
        if (firstLine.contains("{")) {
            structName = firstLine.substring(6, firstLine.indexOf('{')).trim();
        } else {
            structName = firstLine.substring(6).trim();
        }
        
        Map<String, String> fields = new LinkedHashMap<>();
        int currentIndex = startIndex + 1;
        
        // If the opening brace is not on the first line, find it
        if (!firstLine.contains("{")) {
            while (currentIndex < lines.size() && !lines.get(currentIndex).trim().equals("{")) {
                currentIndex++;
            }
            currentIndex++; // Move past the opening brace
        }
        
        // Parse fields until we find the closing brace
        while (currentIndex < lines.size()) {
            String line = lines.get(currentIndex).trim();
            
            if (line.equals("}")) {
                break;
            }
            
            if (line.isEmpty() || line.startsWith("//")) {
                currentIndex++;
                continue;
            }
            
            // Parse field declaration: "var fieldName: Type;"
            if (line.startsWith("var ") && line.contains(":")) {
                String fieldDecl = line.substring(4).trim(); // Remove "var "
                if (fieldDecl.endsWith(";")) {
                    fieldDecl = fieldDecl.substring(0, fieldDecl.length() - 1).trim();
                }
                
                String[] parts = fieldDecl.split(":");
                if (parts.length == 2) {
                    String fieldName = parts[0].trim();
                    String fieldType = parts[1].trim();
                    fields.put(fieldName, fieldType);
                } else {
                    throw new RuntimeException("Invalid field declaration: " + line);
                }
            } else {
                throw new RuntimeException("Invalid syntax in struct definition: " + line);
            }
            
            currentIndex++;
        }
        
        return new Struct(structName, fields);
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public Map<String, String> getFields() {
        return new LinkedHashMap<>(fields);
    }
    
    public Map<String, Object> getValues() {
        return isDefinition ? null : new LinkedHashMap<>(values);
    }
    
    public boolean isDefinition() {
        return isDefinition;
    }
    
    @Override
    public String toString() {
        if (isDefinition) {
            return "struct " + name + " { " + fields.keySet() + " }";
        } else {
            return name + " { " + values + " }";
        }
    }
}