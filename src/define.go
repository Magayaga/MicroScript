// define.go
/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 *
 * It was originally written in Java programming language.
 */
package main

import (
	"regexp"
	"strings"
)

// MacroDef represents a function-like macro (name, parameter list, body)
type MacroDef struct {
	Params []string
	Body   string
}

// Define handles macro preprocessing for MicroScript
type Define struct {
	// Stores object-like macros: NAME -> value
	objectMacros map[string]string
	// Stores function-like macros: NAME -> MacroDef
	functionMacros map[string]*MacroDef
}

// NewDefine creates a new Define instance
func NewDefine() *Define {
	return &Define{
		objectMacros:   make(map[string]string),
		functionMacros: make(map[string]*MacroDef),
	}
}

// Preprocess processes lines for #define macros and expands macros in code
func (d *Define) Preprocess(lines []string) []string {
	var output []string

	for _, line := range lines {
		trimmed := strings.TrimSpace(line)
		if strings.HasPrefix(trimmed, "#define") {
			d.parseDefine(trimmed)
		} else if strings.HasPrefix(trimmed, "#undef") {
			d.parseUndef(trimmed)
		} else {
			// Only expand macros in non-directive lines
			output = append(output, d.ExpandMacros(line))
		}
	}

	return output
}

// parseDefine parses a #define macro line
// Only accepts ALL UPPERCASE macro names (with underscores/numbers)
func (d *Define) parseDefine(line string) {
	// Function-like macro: #define NAME(PARAMS) body (NAME is ALL UPPERCASE)
	funcPat := regexp.MustCompile(`#define\s+([A-Z_][A-Z0-9_]*)\s*\(([^)]*)\)\s*(.*)`)
	if matches := funcPat.FindStringSubmatch(line); matches != nil {
		name := matches[1]
		paramList := strings.TrimSpace(matches[2])
		body := strings.TrimSpace(matches[3])

		var params []string
		if paramList != "" {
			params = strings.Split(paramList, ",")
			for i, param := range params {
				params[i] = strings.TrimSpace(param)
			}
		}

		d.functionMacros[name] = &MacroDef{
			Params: params,
			Body:   body,
		}
		return
	}

	// Object-like macro: #define NAME value (NAME is ALL UPPERCASE)
	objPat := regexp.MustCompile(`#define\s+([A-Z_][A-Z0-9_]*)(?:\s+(.*))?`)
	if matches := objPat.FindStringSubmatch(line); matches != nil {
		name := matches[1]
		value := ""
		if len(matches) > 2 && matches[2] != "" {
			value = strings.TrimSpace(matches[2])
		}
		d.objectMacros[name] = value
	}
}

// parseUndef parses a #undef directive to remove macro definitions
func (d *Define) parseUndef(line string) {
	undefPat := regexp.MustCompile(`#undef\s+([A-Z_][A-Z0-9_]*)`)
	if matches := undefPat.FindStringSubmatch(line); matches != nil {
		name := matches[1]
		delete(d.objectMacros, name)
		delete(d.functionMacros, name)
	}
}

// ExpandMacros expands macros in a single line
// If a function-like macro is called with the wrong number of arguments,
// replaces the macro call with a runtime error marker
func (d *Define) ExpandMacros(line string) string {
	result := line

	// Multiple passes to handle nested macro expansions
	for pass := 0; pass < 10; pass++ { // Limit passes to prevent infinite loops
		beforeExpansion := result

		// Expand function-like macros first
		result = d.expandFunctionMacros(result)

		// Expand object-like macros
		result = d.expandObjectMacros(result)

		// If no changes were made, we're done
		if result == beforeExpansion {
			break
		}
	}

	return result
}

// expandFunctionMacros expands function-like macros in a line
func (d *Define) expandFunctionMacros(line string) string {
	result := line
	replaced := true

	for replaced {
		replaced = false
		for name, macro := range d.functionMacros {
			// Regex to match macro call: NAME(arg1, arg2, ...)
			// Use word boundary to avoid partial matches
			pattern := `\b` + regexp.QuoteMeta(name) + `\s*\(([^()]*(?:\([^()]*\)[^()]*)*)\)`
			callPat := regexp.MustCompile(pattern)

			if match := callPat.FindStringSubmatch(result); match != nil {
				argStr := match[1]
				args := d.splitArgs(argStr)

				if len(args) != len(macro.Params) {
					// Wrong number of arguments, mark as error
					result = callPat.ReplaceAllString(result, "/*MACRO_ARG_ERROR:"+name+"*/")
				} else {
					body := macro.Body

					// Replace parameters with arguments
					for i, param := range macro.Params {
						arg := strings.TrimSpace(args[i])
						paramPat := regexp.MustCompile(`\b` + regexp.QuoteMeta(param) + `\b`)
						body = paramPat.ReplaceAllString(body, arg)
					}

					// Only wrap in parentheses if the body contains operators and isn't already wrapped
					if d.needsParentheses(body) {
						body = "(" + body + ")"
					}

					result = callPat.ReplaceAllString(result, body)
				}
				replaced = true
				break
			}
		}
	}

	return result
}

// expandObjectMacros expands object-like macros in a line
func (d *Define) expandObjectMacros(line string) string {
	result := line

	for name, value := range d.objectMacros {
		// Use word boundary to replace only complete macro names
		pattern := `\b` + regexp.QuoteMeta(name) + `\b`
		objPat := regexp.MustCompile(pattern)
		result = objPat.ReplaceAllString(result, value)
	}

	return result
}

// needsParentheses determines if a macro body needs to be wrapped in parentheses
func (d *Define) needsParentheses(body string) bool {
	if body == "" {
		return false
	}
	if strings.HasPrefix(body, "(") && strings.HasSuffix(body, ")") {
		return false
	}

	// Check if body contains operators that might need precedence protection
	opPat := regexp.MustCompile(`[+\-*/&|^%<>=!]`)
	return opPat.MatchString(body)
}

// splitArgs utility to split macro arguments, respecting nested parentheses and ignoring commas inside them
func (d *Define) splitArgs(argStr string) []string {
	var args []string

	if strings.TrimSpace(argStr) == "" {
		return args
	}

	depth := 0
	var buf strings.Builder
	inQuote := false
	inChar := false

	for i, c := range argStr {
		// Handle string literals
		if c == '"' && !inChar && (i == 0 || argStr[i-1] != '\\') {
			inQuote = !inQuote
		} else if c == '\'' && !inQuote && (i == 0 || argStr[i-1] != '\\') {
			// Handle character literals
			inChar = !inChar
		}

		if !inQuote && !inChar {
			if c == '(' || c == '[' || c == '{' {
				depth++
			}
			if c == ')' || c == ']' || c == '}' {
				depth--
			}

			if c == ',' && depth == 0 {
				args = append(args, strings.TrimSpace(buf.String()))
				buf.Reset()
				continue
			}
		}

		buf.WriteRune(c)
	}

	if buf.Len() > 0 {
		args = append(args, strings.TrimSpace(buf.String()))
	}

	return args
}

// IsDefined checks if a macro is defined (either object-like or function-like)
func (d *Define) IsDefined(name string) bool {
	_, objExists := d.objectMacros[name]
	_, funcExists := d.functionMacros[name]
	return objExists || funcExists
}

// GetObjectMacro gets the value of an object-like macro (returns empty string and false if not defined or is function-like)
func (d *Define) GetObjectMacro(name string) (string, bool) {
	value, exists := d.objectMacros[name]
	return value, exists
}

// GetFunctionMacro gets the definition of a function-like macro (returns nil if not defined or is object-like)
func (d *Define) GetFunctionMacro(name string) *MacroDef {
	return d.functionMacros[name]
}

// Clear clears all macro definitions
func (d *Define) Clear() {
	d.objectMacros = make(map[string]string)
	d.functionMacros = make(map[string]*MacroDef)
}
