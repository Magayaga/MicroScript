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
            } else {
                output.add(line);
            }
        }
        List<String> expanded = new ArrayList<>();
        for (String line : output) {
            expanded.add(expandMacros(line));
        }
        return expanded;
    }

    /**
     * Parses a #define macro line.
     * Only accepts ALL UPPERCASE macro names (with underscores/numbers).
     */
    private void parseDefine(String line) {
        // Function-like macro: #define NAME(PARAMS) body (NAME is ALL UPPERCASE)
        Pattern funcPat = Pattern.compile("#define\\s+([A-Z_][A-Z0-9_]*)\\s*\\(([^)]*)\\)\\s+(.+)");
        Matcher mFunc = funcPat.matcher(line);
        if (mFunc.matches()) {
            String name = mFunc.group(1);
            String paramList = mFunc.group(2).trim();
            List<String> params = paramList.isEmpty() ? new ArrayList<>() :
                Arrays.asList(paramList.split("\\s*,\\s*"));
            String body = mFunc.group(3);
            functionMacros.put(name, new MacroDef(params, body));
            return;
        }
        // Object-like macro: #define NAME value (NAME is ALL UPPERCASE)
        Pattern objPat = Pattern.compile("#define\\s+([A-Z_][A-Z0-9_]*)\\s+(.+)");
        Matcher mObj = objPat.matcher(line);
        if (mObj.matches()) {
            String name = mObj.group(1);
            String value = mObj.group(2);
            objectMacros.put(name, value);
        }
    }

    /**
     * Expands macros in a single line.
     * If a function-like macro is called with the wrong number of arguments,
     * replaces the macro call with a runtime error marker.
     */
    public String expandMacros(String line) {
        // Expand function-like macros first
        boolean replaced;
        do {
            replaced = false;
            for (Map.Entry<String, MacroDef> entry : functionMacros.entrySet()) {
                String macroName = entry.getKey();
                MacroDef def = entry.getValue();
                // Regex: MACRO_NAME(arg1, arg2)
                Pattern callPat = Pattern.compile("\\b" + macroName + "\\s*\\(([^()]*(?:\\([^()]*\\)[^()]*)*)\\)");
                Matcher m = callPat.matcher(line);
                StringBuffer sb = new StringBuffer();
                boolean found = false;
                while (m.find()) {
                    found = true;
                    String argStr = m.group(1);
                    List<String> args = splitArgs(argStr);
                    String replacement;
                    if (args.size() != def.params.size()) {
                        // Error: wrong number of arguments
                        replacement = "__MACRO_ARG_ERROR__(" + macroName + ", expected=" + def.params.size() + ", got=" + args.size() + ")";
                    } else {
                        replacement = def.body;
                        for (int i = 0; i < def.params.size(); i++) {
                            replacement = replacement.replaceAll(
                                "\\b" + Pattern.quote(def.params.get(i)) + "\\b",
                                Matcher.quoteReplacement(args.get(i).trim())
                            );
                        }
                    }
                    m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                }
                m.appendTail(sb);
                if (found) {
                    replaced = true;
                    line = sb.toString();
                }
            }
        } while (replaced); // Support nested macro expansions

        // Expand object-like macros (replace as whole words)
        for (Map.Entry<String, String> entry : objectMacros.entrySet()) {
            String macroName = entry.getKey();
            String macroValue = entry.getValue();
            line = line.replaceAll("\\b" + Pattern.quote(macroName) + "\\b", Matcher.quoteReplacement(macroValue));
        }
        return line;
    }

    /**
     * Utility to split macro arguments, respecting nested parentheses and ignoring commas inside them.
     */
    private List<String> splitArgs(String argStr) {
        List<String> args = new ArrayList<>();
        int depth = 0;
        StringBuilder buf = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < argStr.length(); i++) {
            char c = argStr.charAt(i);
            if (c == '"' && (i == 0 || argStr.charAt(i - 1) != '\\')) {
                inQuote = !inQuote;
            }
            if (!inQuote) {
                if (c == '(') depth++;
                if (c == ')') depth--;
                if (c == ',' && depth == 0) {
                    args.add(buf.toString());
                    buf.setLength(0);
                    continue;
                }
            }
            buf.append(c);
        }
        if (buf.length() > 0) {
            args.add(buf.toString());
        }
        return args;
    }
}