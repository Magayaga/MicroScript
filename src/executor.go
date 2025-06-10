/**
 * MicroScript — The programming language
 * Copyright (c) 2024, 2025 Cyril John Magayaga
 *
 * It was originally written in Go programming language
 */
package main

import (
	"fmt"
	"os"
	"os/exec"
	"regexp"
	"strconv"
	"strings"
)

// Enhanced Executor with complete functionality
type Executor struct {
	environment *Environment
}

// NewExecutor creates a new executor
func NewExecutor(env *Environment) *Executor {
	return &Executor{environment: env}
}

// Execute executes a statement with full functionality
func (e *Executor) Execute(statement string) error {
	statement = strings.TrimSpace(statement)

	// Skip comments
	if strings.HasPrefix(statement, "//") {
		return nil
	}

	// Handle increment/decrement operations
	if e.handleIncrementDecrement(statement) {
		return nil
	}

	// Handle console.write operations
	if strings.HasPrefix(statement, "console.write") {
		return e.handleConsoleWrite(statement)
	}

	// Handle console.system operations
	if strings.HasPrefix(statement, "console.system") {
		return e.handleConsoleSystem(statement)
	}

	// Handle variable declarations
	if strings.HasPrefix(statement, "var ") {
		return e.handleVariableDeclaration(statement)
	}

	// Handle boolean declarations
	if strings.HasPrefix(statement, "bool ") {
		return e.handleBooleanDeclaration(statement)
	}

	// Handle assignments
	if strings.Contains(statement, "=") && !strings.Contains(statement, "==") {
		return e.handleAssignment(statement)
	}

	// Evaluate as expression
	_, err := e.Evaluate(statement)
	return err
}

// handleIncrementDecrement handles ++/-- operations
func (e *Executor) handleIncrementDecrement(statement string) bool {
	// Pre-increment: ++var
	preIncPattern := regexp.MustCompile(`^\+\+([a-zA-Z_][a-zA-Z0-9_]*)\s*;?$`)
	if matches := preIncPattern.FindStringSubmatch(statement); matches != nil {
		varName := matches[1]
		if val, exists := e.environment.GetVariable(varName); exists {
			if num, ok := val.(float64); ok {
				e.environment.SetVariable(varName, num+1)
				return true
			}
			if num, ok := val.(int); ok {
				e.environment.SetVariable(varName, num+1)
				return true
			}
		}
		return true
	}

	// Post-increment: var++
	postIncPattern := regexp.MustCompile(`^([a-zA-Z_][a-zA-Z0-9_]*)\+\+\s*;?$`)
	if matches := postIncPattern.FindStringSubmatch(statement); matches != nil {
		varName := matches[1]
		if val, exists := e.environment.GetVariable(varName); exists {
			if num, ok := val.(float64); ok {
				e.environment.SetVariable(varName, num+1)
				return true
			}
			if num, ok := val.(int); ok {
				e.environment.SetVariable(varName, num+1)
				return true
			}
		}
		return true
	}

	// Pre-decrement: --var
	preDecPattern := regexp.MustCompile(`^--([a-zA-Z_][a-zA-Z0-9_]*)\s*;?$`)
	if matches := preDecPattern.FindStringSubmatch(statement); matches != nil {
		varName := matches[1]
		if val, exists := e.environment.GetVariable(varName); exists {
			if num, ok := val.(float64); ok {
				e.environment.SetVariable(varName, num-1)
				return true
			}
			if num, ok := val.(int); ok {
				e.environment.SetVariable(varName, num-1)
				return true
			}
		}
		return true
	}

	// Post-decrement: var--
	postDecPattern := regexp.MustCompile(`^([a-zA-Z_][a-zA-Z0-9_]*)--\s*;?$`)
	if matches := postDecPattern.FindStringSubmatch(statement); matches != nil {
		varName := matches[1]
		if val, exists := e.environment.GetVariable(varName); exists {
			if num, ok := val.(float64); ok {
				e.environment.SetVariable(varName, num-1)
				return true
			}
			if num, ok := val.(int); ok {
				e.environment.SetVariable(varName, num-1)
				return true
			}
		}
		return true
	}

	return false
}

// handleConsoleWrite handles console.write and console.writef
func (e *Executor) handleConsoleWrite(statement string) error {
	// Handle console.writef (formatted output without newline)
	writefPattern := regexp.MustCompile(`console\.writef\((.*)\);?`)
	if matches := writefPattern.FindStringSubmatch(statement); matches != nil {
		content := strings.TrimSpace(matches[1])
		args := e.splitArguments(content)

		if len(args) == 0 {
			return fmt.Errorf("console.writef() requires at least one argument")
		}

		template := e.Evaluate(args[0])
		templateStr, ok := template.(string)
		if !ok {
			fmt.Print(template)
			return nil
		}

		// Process string templates
		result := e.processStringTemplate(templateStr, args[1:])
		fmt.Print(result)
		return nil
	}

	// Handle console.write (with newline)
	writePattern := regexp.MustCompile(`console\.write\((.*)\);?`)
	if matches := writePattern.FindStringSubmatch(statement); matches != nil {
		content := strings.TrimSpace(matches[1])
		args := e.splitArguments(content)

		if len(args) == 0 {
			return fmt.Errorf("console.write() requires at least one argument")
		}

		template := e.Evaluate(args[0])
		templateStr, ok := template.(string)
		if !ok {
			fmt.Println(template)
			return nil
		}

		// Process string templates
		result := e.processStringTemplate(templateStr, args[1:])
		fmt.Println(result)
		return nil
	}

	return nil
}

// handleConsoleSystem handles console.system calls
func (e *Executor) handleConsoleSystem(statement string) error {
	systemPattern := regexp.MustCompile(`console\.system\((.*)\);?`)
	if matches := systemPattern.FindStringSubmatch(statement); matches != nil {
		content := strings.TrimSpace(matches[1])
		args := e.splitArguments(content)

		if len(args) == 0 {
			return fmt.Errorf("console.system() requires at least one argument")
		}

		command := e.Evaluate(args[0])
		commandStr, ok := command.(string)
		if !ok {
			return fmt.Errorf("console.system() command must be a string")
		}

		// Process string templates in command
		processedCommand := e.processStringTemplate(commandStr, args[1:])

		return e.executeSystemCommand(processedCommand)
	}
	return nil
}

// executeSystemCommand executes a system command
func (e *Executor) executeSystemCommand(command string) error {
	var cmd *exec.Cmd

	// Determine shell based on OS
	if strings.Contains(strings.ToLower(os.Getenv("OS")), "windows") {
		cmd = exec.Command("cmd", "/c", command)
	} else {
		cmd = exec.Command("sh", "-c", command)
	}

	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	return cmd.Run()
}

// handleVariableDeclaration handles var declarations
func (e *Executor) handleVariableDeclaration(statement string) error {
	declaration := strings.TrimSpace(statement[4:]) // Remove "var "

	equalsIndex := strings.Index(declaration, "=")
	if equalsIndex == -1 {
		return fmt.Errorf("syntax error in variable declaration: %s", statement)
	}

	varDeclaration := strings.TrimSpace(declaration[:equalsIndex])
	valueExpression := strings.TrimSpace(strings.Replace(declaration[equalsIndex+1:], ";", "", -1))

	// Parse type annotation
	parts := strings.Split(varDeclaration, ":")
	if len(parts) != 2 {
		return fmt.Errorf("syntax error in variable declaration: %s", statement)
	}

	varName := strings.TrimSpace(parts[0])
	typeAnnotation := strings.TrimSpace(parts[1])

	value := e.Evaluate(valueExpression)

	// Type checking
	if err := e.validateType(value, typeAnnotation); err != nil {
		return err
	}

	e.environment.SetVariable(varName, value)
	return nil
}

// handleBooleanDeclaration handles bool declarations
func (e *Executor) handleBooleanDeclaration(statement string) error {
	declaration := strings.TrimSpace(statement[5:]) // Remove "bool "

	equalsIndex := strings.Index(declaration, "=")
	if equalsIndex == -1 {
		return fmt.Errorf("syntax error in boolean declaration: %s", statement)
	}

	boolName := strings.TrimSpace(declaration[:equalsIndex])
	valueExpression := strings.TrimSpace(strings.Replace(declaration[equalsIndex+1:], ";", "", -1))

	value := e.Evaluate(valueExpression)
	if _, ok := value.(bool); !ok {
		return fmt.Errorf("syntax error: %s is not a boolean", valueExpression)
	}

	e.environment.SetVariable(boolName, value)
	return nil
}

// handleAssignment handles variable assignments
func (e *Executor) handleAssignment(statement string) error {
	equalsIndex := strings.Index(statement, "=")
	varName := strings.TrimSpace(statement[:equalsIndex])
	valueExpression := strings.TrimSpace(strings.Replace(statement[equalsIndex+1:], ";", "", -1))

	value := e.Evaluate(valueExpression)
	e.environment.SetVariable(varName, value)
	return nil
}

// validateType validates that a value matches the expected type
func (e *Executor) validateType(value interface{}, expectedType string) error {
	switch expectedType {
	case "String":
		if _, ok := value.(string); !ok {
			return fmt.Errorf("type error: value is not a String")
		}
	case "Int32", "Int64":
		if _, ok := value.(int); !ok {
			return fmt.Errorf("type error: value is not an Integer")
		}
	case "Float32", "Float64":
		if _, ok := value.(float64); !ok {
			if _, ok := value.(int); !ok {
				return fmt.Errorf("type error: value is not a Float")
			}
		}
	case "Char":
		if str, ok := value.(string); !ok || len(str) != 1 {
			return fmt.Errorf("type error: value is not a Character")
		}
	default:
		return fmt.Errorf("unknown type annotation: %s", expectedType)
	}
	return nil
}

// processStringTemplate processes string templates with {expression} and {} placeholders
func (e *Executor) processStringTemplate(template string, args []string) string {
	// Process escape sequences
	template = e.processEscapeSequences(template)

	// Process {expression} style placeholders
	exprPattern := regexp.MustCompile(`\{([^{}]+)\}`)
	template = exprPattern.ReplaceAllStringFunc(template, func(match string) string {
		expr := match[1 : len(match)-1] // Remove { and }
		result := e.Evaluate(expr)
		return fmt.Sprintf("%v", result)
	})

	// Process {} style positional placeholders
	positionalPattern := regexp.MustCompile(`\{\}`)
	argIndex := 0
	template = positionalPattern.ReplaceAllStringFunc(template, func(match string) string {
		if argIndex < len(args) {
			result := e.Evaluate(args[argIndex])
			argIndex++
			return fmt.Sprintf("%v", result)
		}
		return match
	})

	return template
}

// processEscapeSequences processes escape sequences in strings
func (e *Executor) processEscapeSequences(input string) string {
	result := strings.Builder{}

	for i := 0; i < len(input); i++ {
		if input[i] == '\\' && i+1 < len(input) {
			switch input[i+1] {
			case 'n':
				result.WriteByte('\n')
				i++ // Skip next character
			case 't':
				result.WriteByte('\t')
				i++
			case 'r':
				result.WriteByte('\r')
				i++
			case '\\':
				result.WriteByte('\\')
				i++
			case '"':
				result.WriteByte('"')
				i++
			case '\'':
				result.WriteByte('\'')
				i++
			case '0':
				result.WriteByte('\000')
				i++
			default:
				result.WriteByte(input[i])
			}
		} else {
			result.WriteByte(input[i])
		}
	}

	return result.String()
}

// splitArguments splits function arguments respecting quotes and parentheses
func (e *Executor) splitArguments(content string) []string {
	var result []string
	var current strings.Builder
	inQuotes := false
	level := 0

	for i, c := range content {
		switch c {
		case '"':
			if i == 0 || content[i-1] != '\\' {
				inQuotes = !inQuotes
			}
			current.WriteRune(c)
		case '(':
			if !inQuotes {
				level++
			}
			current.WriteRune(c)
		case ')':
			if !inQuotes {
				level--
			}
			current.WriteRune(c)
		case ',':
			if !inQuotes && level == 0 {
				result = append(result, strings.TrimSpace(current.String()))
				current.Reset()
			} else {
				current.WriteRune(c)
			}
		default:
			current.WriteRune(c)
		}
	}

	if current.Len() > 0 {
		result = append(result, strings.TrimSpace(current.String()))
	}

	return result
}

// Enhanced Evaluate method with complete expression evaluation
func (e *Executor) Evaluate(expression string) interface{} {
	if expression == "" {
		return nil
	}

	expression = strings.TrimSpace(expression)

	// String literals
	if strings.HasPrefix(expression, "\"") && strings.HasSuffix(expression, "\"") {
		return expression[1 : len(expression)-1]
	}

	// Character literals
	if strings.HasPrefix(expression, "'") && strings.HasSuffix(expression, "'") && len(expression) == 3 {
		return string(expression[1])
	}

	// Boolean literals
	if expression == "true" {
		return true
	}
	if expression == "false" {
		return false
	}

	// Boolean negation
	if strings.HasPrefix(expression, "not ") {
		value := e.Evaluate(expression[4:])
		if b, ok := value.(bool); ok {
			return !b
		}
		return false
	}

	if strings.HasPrefix(expression, "!") {
		value := e.Evaluate(expression[1:])
		if b, ok := value.(bool); ok {
			return !b
		}
		return false
	}

	// Numeric literals
	if val, err := strconv.Atoi(expression); err == nil {
		return val
	}
	if val, err := strconv.ParseFloat(expression, 64); err == nil {
		return val
	}

	// Function calls
	funcPattern := regexp.MustCompile(`(\w+)\((.*)\)`)
	if matches := funcPattern.FindStringSubmatch(expression); matches != nil {
		functionName := matches[1]
		args := strings.TrimSpace(matches[2])
		var arguments []string
		if args != "" {
			arguments = e.splitArguments(args)
		}
		result, _ := e.ExecuteFunction(functionName, arguments)
		return result
	}

	// Variable lookup
	if val, exists := e.environment.GetVariable(expression); exists {
		return val
	}

	// Ternary operator
	if strings.Contains(expression, "?") && strings.Contains(expression, ":") {
		return e.evaluateTernary(expression)
	}

	// Arithmetic expressions
	return e.evaluateArithmetic(expression)
}

// evaluateTernary evaluates ternary expressions
func (e *Executor) evaluateTernary(expression string) interface{} {
	questionPos := strings.Index(expression, "?")
	colonPos := strings.Index(expression, ":")

	if questionPos > 0 && colonPos > questionPos {
		condition := strings.TrimSpace(expression[:questionPos])
		trueExpr := strings.TrimSpace(expression[questionPos+1 : colonPos])
		falseExpr := strings.TrimSpace(expression[colonPos+1:])

		condValue := e.Evaluate(condition)
		var condResult bool

		switch v := condValue.(type) {
		case bool:
			condResult = v
		case int:
			condResult = v != 0
		case float64:
			condResult = v != 0.0
		default:
			condResult = false
		}

		if condResult {
			return e.Evaluate(trueExpr)
		}
		return e.Evaluate(falseExpr)
	}

	return expression
}

// evaluateArithmetic evaluates simple arithmetic expressions
func (e *Executor) evaluateArithmetic(expression string) interface{} {
	// This is a simplified arithmetic evaluator
	// A full implementation would need a proper expression parser

	// Handle simple binary operations
	for _, op := range []string{"+", "-", "*", "/", "%"} {
		if strings.Contains(expression, op) {
			parts := strings.Split(expression, op)
			if len(parts) == 2 {
				left := e.Evaluate(strings.TrimSpace(parts[0]))
				right := e.Evaluate(strings.TrimSpace(parts[1]))

				return e.performArithmetic(left, right, op)
			}
		}
	}

	return expression
}

// performArithmetic performs arithmetic operations
func (e *Executor) performArithmetic(left, right interface{}, op string) interface{} {
	// Convert to numbers if possible
	leftNum := e.toNumber(left)
	rightNum := e.toNumber(right)

	if leftNum != nil && rightNum != nil {
		l := *leftNum
		r := *rightNum

		switch op {
		case "+":
			return l + r
		case "-":
			return l - r
		case "*":
			return l * r
		case "/":
			if r != 0 {
				return l / r
			}
		case "%":
			if r != 0 {
				return float64(int(l) % int(r))
			}
		}
	}

	return 0
}

// toNumber converts a value to a number if possible
func (e *Executor) toNumber(value interface{}) *float64 {
	switch v := value.(type) {
	case int:
		f := float64(v)
		return &f
	case float64:
		return &v
	case string:
		if f, err := strconv.ParseFloat(v, 64); err == nil {
			return &f
		}
	}
	return nil
}

// ExecuteFunction executes a function with enhanced functionality
func (e *Executor) ExecuteFunction(name string, args []string) (interface{}, error) {
	function := e.environment.GetFunction(name)
	if function == nil {
		return nil, fmt.Errorf("function not found: %s", name)
	}

	// Create local environment
	localEnv := NewChildEnvironment(e.environment)

	// Bind parameters
	if len(function.Parameters) != len(args) {
		return nil, fmt.Errorf("argument count mismatch for function: %s", name)
	}

	for i, param := range function.Parameters {
		value := e.Evaluate(args[i])
		if err := e.validateType(value, param.Type); err != nil {
			return nil, fmt.Errorf("type error for parameter %s: %v", param.Name, err)
		}
		localEnv.SetVariable(param.Name, value)
	}

	// Execute function body
	localExecutor := NewExecutor(localEnv)
	var returnValue interface{}

	for _, line := range function.Body {
		line = strings.TrimSpace(line)
		if line == "" || strings.HasPrefix(line, "//") {
			continue
		}

		if strings.HasPrefix(line, "return") {
			returnExpr := strings.TrimSpace(line[6:])
			returnExpr = strings.TrimSuffix(returnExpr, ";")
			returnValue = localExecutor.Evaluate(returnExpr)
			break
		}

		if err := localExecutor.Execute(line); err != nil {
			return nil, err
		}
	}

	return returnValue, nil
}
