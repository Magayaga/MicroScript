/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.*;
import java.util.regex.*;

public class Define {
    // Stores object-like macros: NAME -> value
    private final Map<String, String> objectMacros = new HashMap<>();
    // Stores function-like macros: NAME -> MacroDef
    private final Map<String, MacroDef> functionMacros = new HashMap<>();

    // Represents a function-like macro (name, parameter list, body)
    private static class MacroDef {
        final List<String> params;
        final String body;
        MacroDef(List<String> params, String body) {
            this.params = params;
            this.body = body;
        }
    }

    /**
     * Processes lines for #define macros and expands macros in code.
     */
    public List<String> preprocess(List<String> lines) {
        List<String> output = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("#define")) {
                parseDefine(trimmed);
            } else if (trimmed.startsWith("#undef")) {
                parseUndef(trimmed);
            } else {
                // Only expand macros in non-directive lines
                output.add(expandMacros(line));
            }
        }
        return output;
    }

    /**
     * Parses a #define macro line.
     * Only accepts ALL UPPERCASE macro names (with underscores/numbers).
     */
    private void parseDefine(String line) {
        // Function-like macro: #define NAME(PARAMS) body (NAME is ALL UPPERCASE)
        Pattern funcPat = Pattern.compile("#define\\s+([A-Z_][A-Z0-9_]*)\\s*\\(([^)]*)\\)\\s*(.*)");
        Matcher mFunc = funcPat.matcher(line);
        if (mFunc.matches()) {
            String name = mFunc.group(1);
            String paramList = mFunc.group(2).trim();
            List<String> params = paramList.isEmpty() ? new ArrayList<>() :
                Arrays.asList(paramList.split("\\s*,\\s*"));
            String body = mFunc.group(3).trim();
            // Allow empty body for function-like macros
            functionMacros.put(name, new MacroDef(params, body));
            return;
        }
        // Object-like macro: #define NAME value (NAME is ALL UPPERCASE)
        Pattern objPat = Pattern.compile("#define\\s+([A-Z_][A-Z0-9_]*)(?:\\s+(.*))?");
        Matcher mObj = objPat.matcher(line);
        if (mObj.matches()) {
            String name = mObj.group(1);
            String value = mObj.group(2);
            // Allow empty value for object-like macros (equivalent to empty string)
            objectMacros.put(name, value != null ? value.trim() : "");
        }
    }

    /**
     * Parses a #undef directive to remove macro definitions.
     */
    private void parseUndef(String line) {
        Pattern undefPat = Pattern.compile("#undef\\s+([A-Z_][A-Z0-9_]*)");
        Matcher mUndef = undefPat.matcher(line);
        if (mUndef.matches()) {
            String name = mUndef.group(1);
            objectMacros.remove(name);
            functionMacros.remove(name);
        }
    }

    /**
     * Expands macros in a single line.
     * If a function-like macro is called with the wrong number of arguments,
     * replaces the macro call with a runtime error marker.
     */
    public String expandMacros(String line) {
        String result = line;

        // Multiple passes to handle nested macro expansions
        for (int pass = 0; pass < 10; pass++) { // Limit passes to prevent infinite loops
            String beforeExpansion = result;

            // Expand function-like macros first
            result = expandFunctionMacros(result);

            // Expand object-like macros
            result = expandObjectMacros(result);

            // If no changes were made, we're done
            if (result.equals(beforeExpansion)) {
                break;
            }
        }

        return result;
    }

    /**
     * Expands function-like macros in a line.
     */
    private String expandFunctionMacros(String line) {
        String result = line;
        boolean replaced;

        do {
            replaced = false;
            for (Map.Entry<String, MacroDef> entry : functionMacros.entrySet()) {
                String name = entry.getKey();
                MacroDef macro = entry.getValue();

                // Regex to match macro call: NAME(arg1, arg2, ...)
                // Use word boundary to avoid partial matches
                Pattern callPat = Pattern.compile("\\b" + Pattern.quote(name) + "\\s*\\(([^()]*(?:\\([^()]*\\)[^()]*)*)\\)");
                Matcher mCall = callPat.matcher(result);

                if (mCall.find()) {
                    String argStr = mCall.group(1);
                    List<String> args = splitArgs(argStr);

                    if (args.size() != macro.params.size()) {
                        // Wrong number of arguments, mark as error
                        result = mCall.replaceFirst("/*MACRO_ARG_ERROR:" + name + "*/");
                    } else {
                        String body = macro.body;

                        // Replace parameters with arguments
                        for (int i = 0; i < macro.params.size(); i++) {
                            String param = macro.params.get(i).trim();
                            String arg = args.get(i).trim();
                            // Use word boundary to replace only complete parameter names
                            body = body.replaceAll("\\b" + Pattern.quote(param) + "\\b",
                                                 Matcher.quoteReplacement(arg));
                        }

                        // Only wrap in parentheses if the body contains operators and isn't already wrapped
                        if (needsParentheses(body)) {
                            body = "(" + body + ")";
                        }

                        result = mCall.replaceFirst(Matcher.quoteReplacement(body));
                    }
                    replaced = true;
                    break;
                }
            }
        } while (replaced);

        return result;
    }

    /**
     * Expands object-like macros in a line.
     */
    private String expandObjectMacros(String line) {
        String result = line;

        for (Map.Entry<String, String> entry : objectMacros.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            // Use word boundary to replace only complete macro names
            result = result.replaceAll("\\b" + Pattern.quote(name) + "\\b",
                                     Matcher.quoteReplacement(value));
        }

        return result;
    }

    /**
     * Determines if a macro body needs to be wrapped in parentheses.
     */
    private boolean needsParentheses(String body) {
        if (body.isEmpty()) return false;
        if (body.startsWith("(") && body.endsWith(")")) return false;

        // Check if body contains operators that might need precedence protection
        return body.matches(".*[+\\-*/&|^%<>=!].*");
    }

    /**
     * Utility to split macro arguments, respecting nested parentheses and ignoring commas inside them.
     */
    private List<String> splitArgs(String argStr) {
        List<String> args = new ArrayList<>();

        if (argStr.trim().isEmpty()) {
            return args;
        }

        int depth = 0;
        StringBuilder buf = new StringBuilder();
        boolean inQuote = false;
        boolean inChar = false;

        for (int i = 0; i < argStr.length(); i++) {
            char c = argStr.charAt(i);

            // Handle string literals
            if (c == '"' && !inChar && (i == 0 || argStr.charAt(i - 1) != '\\')) {
                inQuote = !inQuote;
            }
            // Handle character literals
            else if (c == '\'' && !inQuote && (i == 0 || argStr.charAt(i - 1) != '\\')) {
                inChar = !inChar;
            }

            if (!inQuote && !inChar) {
                if (c == '(' || c == '[' || c == '{') depth++;
                if (c == ')' || c == ']' || c == '}') depth--;

                if (c == ',' && depth == 0) {
                    args.add(buf.toString().trim());
                    buf.setLength(0);
                    continue;
                }
            }

            buf.append(c);
        }

        if (buf.length() > 0) {
            args.add(buf.toString().trim());
        }

        return args;
    }

    /**
     * Checks if a macro is defined (either object-like or function-like).
     */
    public boolean isDefined(String name) {
        return objectMacros.containsKey(name) || functionMacros.containsKey(name);
    }

    /**
     * Gets the value of an object-like macro (returns null if not defined or is function-like).
     */
    public String getObjectMacro(String name) {
        return objectMacros.get(name);
    }

    /**
     * Gets the definition of a function-like macro (returns null if not defined or is object-like).
     */
    public MacroDef getFunctionMacro(String name) {
        return functionMacros.get(name);
    }

    /**
     * Clears all macro definitions.
     */
    public void clear() {
        objectMacros.clear();
        functionMacros.clear();
    }
}