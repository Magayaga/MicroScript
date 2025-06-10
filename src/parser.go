/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 *
 * It was originally written in Go programming language
 */
package main

import (
	"fmt"
	"regexp"
	"strconv"
	"strings"
)

// Parameter represents a function parameter
type Parameter struct {
	Name string
	Type string
}

// Function represents a MicroScript function
type Function struct {
	Name       string
	Parameters []Parameter
	ReturnType string
	Body       []string
}

// ArrowFunction represents an arrow function
type ArrowFunction struct {
	Name       string
	Parameters []Parameter
	ReturnType string
	Body       string
	IsLambda   bool
}

// Environment manages variables and functions
type Environment struct {
	variables map[string]interface{}
	functions map[string]*Function
	parent    *Environment
}

// NewEnvironment creates a new environment
func NewEnvironment() *Environment {
	return &Environment{
		variables: make(map[string]interface{}),
		functions: make(map[string]*Function),
	}
}

// NewChildEnvironment creates a child environment
func NewChildEnvironment(parent *Environment) *Environment {
	return &Environment{
		variables: make(map[string]interface{}),
		functions: make(map[string]*Function),
		parent:    parent,
	}
}

// SetVariable sets a variable in the environment
func (e *Environment) SetVariable(name string, value interface{}) {
	e.variables[name] = value
}

// GetVariable gets a variable from the environment
func (e *Environment) GetVariable(name string) (interface{}, bool) {
	if value, exists := e.variables[name]; exists {
		return value, true
	}
	if e.parent != nil {
		return e.parent.GetVariable(name)
	}
	return nil, false
}

// DefineFunction defines a function in the environment
func (e *Environment) DefineFunction(function *Function) {
	e.functions[function.Name] = function
}

// GetFunction gets a function from the environment
func (e *Environment) GetFunction(name string) *Function {
	if function, exists := e.functions[name]; exists {
		return function
	}
	if e.parent != nil {
		return e.parent.GetFunction(name)
	}
	return nil
}

// Parser represents the MicroScript parser
type Parser struct {
	lines       []string
	environment *Environment
}

// NewParser creates a new parser with preprocessed lines
func NewParser(lines []string) *Parser {
	return &Parser{
		lines:       lines,
		environment: NewEnvironment(),
	}
}

// Parse parses and executes the MicroScript code
func (p *Parser) Parse() error {
	i := 0
	hasCStyleMain := false

	for i < len(p.lines) {
		line := strings.TrimSpace(p.lines[i])

		// Skip comments and empty lines
		if strings.HasPrefix(line, "//") || line == "" {
			i++
			continue
		}

		// Skip multi-line comments
		if strings.HasPrefix(line, "/*") {
			for i < len(p.lines) && !strings.Contains(p.lines[i], "*/") {
				i++
			}
			if i < len(p.lines) {
				i++
			}
			continue
		}

		// C-style function
		if matched, _ := regexp.MatchString(`^(String|Int32|Int64|Float32|Float64|fn)\s+\w+\s*\(.*\)\s*\{`, line); matched {
			closingBraceIndex := p.findClosingBrace(i)
			if closingBraceIndex == -1 {
				return fmt.Errorf("missing closing brace for function at line %d", i+1)
			}
			err := p.parseFunction(i, closingBraceIndex)
			if err != nil {
				return err
			}
			if strings.HasPrefix(line, "fn main") {
				hasCStyleMain = true
			}
			i = closingBraceIndex + 1
		} else if strings.HasPrefix(line, "function ") {
			// MicroScript-style function
			closingBraceIndex := p.findClosingBrace(i)
			if closingBraceIndex == -1 {
				return fmt.Errorf("missing closing brace for function at line %d", i+1)
			}
			err := p.parseFunction(i, closingBraceIndex)
			if err != nil {
				return err
			}
			i = closingBraceIndex + 1
		} else if strings.Contains(line, "=>") {
			// Arrow function
			err := p.parseArrowFunction(line)
			if err != nil {
				return err
			}
			i++
		} else if strings.HasPrefix(line, "if") {
			// Conditional statement
			afterConditional := p.processConditionalStatement(i)
			i = afterConditional
		} else if strings.HasPrefix(line, "while") {
			// While loop
			afterLoop := p.processLoop(i)
			i = afterLoop
		} else {
			// Execute top-level commands
			err := p.parseLine(line)
			if err != nil {
				return err
			}
			i++
		}
	}

	// Auto-execute C-style main if present
	if hasCStyleMain {
		mainFunc := p.environment.GetFunction("main")
		if mainFunc != nil {
			executor := NewExecutor(p.environment)
			return executor.ExecuteFunction("main", []string{})
		}
	}

	return nil
}

// parseFunction parses a function definition
func (p *Parser) parseFunction(start, end int) error {
	header := strings.TrimSpace(p.lines[start])

	// C-style function pattern
	cStylePattern := regexp.MustCompile(`^(String|Int32|Int64|Float32|Float64|fn)\s+(\w+)\s*\(([^)]*)\)\s*\{`)
	if matches := cStylePattern.FindStringSubmatch(header); matches != nil {
		returnType := matches[1]
		name := matches[2]
		params := strings.TrimSpace(matches[3])
		return p.parseFunctionBody(name, params, returnType, start, end)
	}

	// MicroScript-style function pattern
	microScriptPattern := regexp.MustCompile(`function\s+(\w+)\s*\(([^)]*)\)\s*(?:->\s*(\w+))?\s*\{`)
	if matches := microScriptPattern.FindStringSubmatch(header); matches != nil {
		name := matches[1]
		params := strings.TrimSpace(matches[2])
		returnType := "void"
		if matches[3] != "" {
			returnType = strings.TrimSpace(matches[3])
		}
		return p.parseFunctionBody(name, params, returnType, start, end)
	}

	return fmt.Errorf("invalid function declaration syntax at line %d", start+1)
}

// parseFunctionBody parses the body of a function
func (p *Parser) parseFunctionBody(name, params, returnType string, start, end int) error {
	var parameters []Parameter

	if params != "" {
		paramParts := strings.Split(params, ",")
		for _, param := range paramParts {
			parts := strings.Split(strings.TrimSpace(param), ":")
			if len(parts) != 2 {
				return fmt.Errorf("invalid parameter declaration in function %s", name)
			}
			parameters = append(parameters, Parameter{
				Name: strings.TrimSpace(parts[0]),
				Type: strings.TrimSpace(parts[1]),
			})
		}
	}

	var body []string
	for i := start + 1; i < end; i++ {
		body = append(body, strings.TrimSpace(p.lines[i]))
	}

	function := &Function{
		Name:       name,
		Parameters: parameters,
		ReturnType: returnType,
		Body:       body,
	}

	p.environment.DefineFunction(function)
	return nil
}

// parseArrowFunction parses arrow function syntax
func (p *Parser) parseArrowFunction(line string) error {
	// Pattern for arrow functions with block body
	blockPattern := regexp.MustCompile(`var\s+(\w+)\s*=\s*\|(.*?)\|\s*=>\s*(\w+)?\s*\{(.*?)\};`)
	if matches := blockPattern.FindStringSubmatch(line); matches != nil {
		name := strings.TrimSpace(matches[1])
		paramString := strings.TrimSpace(matches[2])
		returnType := matches[3]
		if returnType == "" {
			returnType = "void"
		}
		body := strings.TrimSpace(matches[4])

		parameters := p.parseArrowFunctionParams(paramString)

		arrowFunc := &ArrowFunction{
			Name:       name,
			Parameters: parameters,
			ReturnType: returnType,
			Body:       body,
			IsLambda:   true,
		}

		// Store as both function and variable
		p.environment.SetVariable(name, arrowFunc)
		return nil
	}

	// Pattern for arrow functions with expression body
	exprPattern := regexp.MustCompile(`var\s+(\w+)\s*=\s*\|(.*?)\|\s*=>\s*([^{][^;]*);`)
	if matches := exprPattern.FindStringSubmatch(line); matches != nil {
		name := strings.TrimSpace(matches[1])
		paramString := strings.TrimSpace(matches[2])
		expression := strings.TrimSpace(matches[3])

		parameters := p.parseArrowFunctionParams(paramString)

		arrowFunc := &ArrowFunction{
			Name:       name,
			Parameters: parameters,
			ReturnType: "void", // Could be inferred
			Body:       expression,
			IsLambda:   true,
		}

		p.environment.SetVariable(name, arrowFunc)
		return nil
	}

	return fmt.Errorf("invalid arrow function syntax: %s", line)
}

// parseArrowFunctionParams parses arrow function parameters
func (p *Parser) parseArrowFunctionParams(paramString string) []Parameter {
	var parameters []Parameter

	if paramString == "&" || paramString == "" {
		return parameters
	}

	paramParts := strings.Split(paramString, ",")
	for _, param := range paramParts {
		typeAndName := strings.Split(strings.TrimSpace(param), ":")
		if len(typeAndName) == 2 {
			parameters = append(parameters, Parameter{
				Name: strings.TrimSpace(typeAndName[1]),
				Type: strings.TrimSpace(typeAndName[0]),
			})
		}
	}

	return parameters
}

// processConditionalStatement processes if/elif/else chains
func (p *Parser) processConditionalStatement(startIndex int) int {
	executor := NewExecutor(p.environment)

	// Simple implementation - just skip the conditional block for now
	// In a full implementation, this would evaluate conditions and execute appropriate blocks
	closingBrace := p.findClosingBrace(startIndex)
	if closingBrace == -1 {
		return startIndex + 1
	}

	// Check for elif/else blocks
	currentIndex := closingBrace + 1
	for currentIndex < len(p.lines) {
		line := strings.TrimSpace(p.lines[currentIndex])
		if strings.HasPrefix(line, "elif") || strings.HasPrefix(line, "else") {
			nextBrace := p.findClosingBrace(currentIndex)
			if nextBrace == -1 {
				break
			}
			currentIndex = nextBrace + 1
		} else {
			break
		}
	}

	return currentIndex
}

// processLoop processes while loops
func (p *Parser) processLoop(startIndex int) int {
	// Simple implementation - just skip the loop for now
	// In a full implementation, this would evaluate the condition and execute the loop body
	closingBrace := p.findClosingBrace(startIndex)
	if closingBrace == -1 {
		return startIndex + 1
	}
	return closingBrace + 1
}

// findClosingBrace finds the matching closing brace
func (p *Parser) findClosingBrace(start int) int {
	openBraces := 0
	for i := start; i < len(p.lines); i++ {
		line := p.lines[i]
		openBraces += strings.Count(line, "{")
		openBraces -= strings.Count(line, "}")
		if openBraces == 0 {
			return i
		}
	}
	return -1
}

// parseLine parses and executes a single line
func (p *Parser) parseLine(line string) error {
	line = strings.TrimSpace(line)

	if strings.HasPrefix(line, "import ") {
		// Handle imports
		moduleName := strings.TrimSpace(line[7:])
		return p.importModule(moduleName)
	}

	// Skip elif/else at top level
	if strings.HasPrefix(line, "elif") || strings.HasPrefix(line, "else") {
		return nil
	}

	// Console output patterns
	consoleWritePattern := regexp.MustCompile(`console\.write\((.*)\);`)
	if matches := consoleWritePattern.FindStringSubmatch(line); matches != nil {
		executor := NewExecutor(p.environment)
		return executor.Execute("console.write(" + matches[1] + ")")
	}

	consoleWritefPattern := regexp.MustCompile(`console\.writef\((.*)\);`)
	if matches := consoleWritefPattern.FindStringSubmatch(line); matches != nil {
		executor := NewExecutor(p.environment)
		return executor.Execute("console.writef(" + matches[1] + ")")
	}

	// IO patterns
	ioPattern := regexp.MustCompile(`io::(print|println)\((.*)\);`)
	if matches := ioPattern.FindStringSubmatch(line); matches != nil {
		functionName := "io::" + matches[1]
		args := strings.TrimSpace(matches[2])
		executor := NewExecutor(p.environment)

		if args == "" {
			return executor.ExecuteFunction(functionName, []string{})
		}
		return executor.ExecuteFunction(functionName, strings.Split(args, ","))
	}

	// Function calls
	callPattern := regexp.MustCompile(`(\w+)\((.*)\);`)
	if matches := callPattern.FindStringSubmatch(line); matches != nil {
		functionName := matches[1]
		args := strings.TrimSpace(matches[2])
		executor := NewExecutor(p.environment)

		if args == "" {
			return executor.ExecuteFunction(functionName, []string{})
		}
		return executor.ExecuteFunction(functionName, strings.Split(args, ","))
	}

	// Variable declarations
	if strings.HasPrefix(line, "var ") || strings.HasPrefix(line, "bool ") {
		executor := NewExecutor(p.environment)
		return executor.Execute(line)
	}

	// Assignments
	if strings.Contains(line, "=") {
		equalsIndex := strings.Index(line, "=")
		varName := strings.TrimSpace(line[:equalsIndex])
		valueExpression := strings.TrimSpace(strings.Replace(line[equalsIndex+1:], ";", "", -1))
		executor := NewExecutor(p.environment)
		return executor.Execute(varName + " = " + valueExpression)
	}

	return nil
}

// importModule handles module imports
func (p *Parser) importModule(moduleName string) error {
	// Placeholder for import functionality
	// In a full implementation, this would load and parse external modules
	fmt.Printf("Importing module: %s\n", moduleName)
	return nil
}

// ExecuteFunction executes a function call
func (e *Executor) ExecuteFunction(name string, args []string) error {
	// Placeholder for function execution
	fmt.Printf("Executing function: %s with args: %v\n", name, args)
	return nil
}

// Evaluate evaluates an expression
func (e *Executor) Evaluate(expression string) interface{} {
	// Simple evaluation - in a full implementation this would parse and evaluate expressions
	if val, err := strconv.Atoi(expression); err == nil {
		return val
	}
	if val, err := strconv.ParseFloat(expression, 64); err == nil {
		return val
	}
	if expression == "true" {
		return true
	}
	if expression == "false" {
		return false
	}
	if strings.HasPrefix(expression, "\"") && strings.HasSuffix(expression, "\"") {
		return expression[1 : len(expression)-1]
	}

	// Try to get from environment
	if val, exists := e.environment.GetVariable(expression); exists {
		return val
	}

	return expression
}
