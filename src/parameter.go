/**
 * MicroScript — The programming language
 * Copyright (c) 2024, 2025 Cyril John Magayaga
 *
 * It was originally written in Go programming language
 */
package main

// Parameter represents a function parameter
type Parameter struct {
	Name string
	Type string
}

// NewParameter creates a new parameter
func NewParameter(name, paramType string) *Parameter {
	return &Parameter{
		Name: name,
		Type: paramType,
	}
}

// GetName returns the parameter name
func (p *Parameter) GetName() string {
	return p.Name
}

// GetType returns the parameter type
func (p *Parameter) GetType() string {
	return p.Type
}
