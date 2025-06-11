/**
 * MicroScript — The programming language
 * Copyright (c) 2024, 2025 Cyril John Magayaga
 *
 * It was originally written in Go programming language
 */
package main

import "strings"

// ListVariable represents a list/array variable in MicroScript
type ListVariable struct {
	elements []interface{}
}

// NewListVariable creates a new empty list variable
func NewListVariable() *ListVariable {
	return &ListVariable{
		elements: make([]interface{}, 0),
	}
}

// NewListVariableFromStrings creates a new list variable from string elements
func NewListVariableFromStrings(elements []string) *ListVariable {
	lv := &ListVariable{
		elements: make([]interface{}, len(elements)),
	}

	for i, element := range elements {
		lv.elements[i] = strings.TrimSpace(element)
	}

	return lv
}

// Add appends an element to the list
func (lv *ListVariable) Add(element interface{}) {
	lv.elements = append(lv.elements, element)
}

// Get returns the element at the specified index
func (lv *ListVariable) Get(index int) interface{} {
	if index >= 0 && index < len(lv.elements) {
		return lv.elements[index]
	}
	return nil
}

// Set sets the element at the specified index
func (lv *ListVariable) Set(index int, element interface{}) bool {
	if index >= 0 && index < len(lv.elements) {
		lv.elements[index] = element
		return true
	}
	return false
}

// Size returns the number of elements in the list
func (lv *ListVariable) Size() int {
	return len(lv.elements)
}

// IsEmpty returns true if the list is empty
func (lv *ListVariable) IsEmpty() bool {
	return len(lv.elements) == 0
}

// Clear removes all elements from the list
func (lv *ListVariable) Clear() {
	lv.elements = lv.elements[:0]
}

// Remove removes the element at the specified index
func (lv *ListVariable) Remove(index int) bool {
	if index >= 0 && index < len(lv.elements) {
		lv.elements = append(lv.elements[:index], lv.elements[index+1:]...)
		return true
	}
	return false
}

// ToSlice returns the underlying slice
func (lv *ListVariable) ToSlice() []interface{} {
	result := make([]interface{}, len(lv.elements))
	copy(result, lv.elements)
	return result
}
