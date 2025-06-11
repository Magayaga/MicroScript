/**
 * MicroScript — The programming language
 * Copyright (c) 2024, 2025 Cyril John Magayaga
 *
 * It was originally written in Go programming language
 */
package main

// Function represents a function definition in MicroScript
type Function struct {
	Name       string
	Parameters []Parameter
	ReturnType string
	Body       []string
}

// NewFunction creates a new function
func NewFunction(name string, parameters []Parameter, returnType string, body []string) *Function {
	return &Function{
		Name:       name,
		Parameters: parameters,
		ReturnType: returnType,
		Body:       body,
	}
}

// GetName returns the function name
func (f *Function) GetName() string {
	return f.Name
}

// GetParameters returns the function parameters
func (f *Function) GetParameters() []Parameter {
	return f.Parameters
}

// GetReturnType returns the function return type
func (f *Function) GetReturnType() string {
	return f.ReturnType
}

// GetBody returns the function body
func (f *Function) GetBody() []string {
	return f.Body
}
