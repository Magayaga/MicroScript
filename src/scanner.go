/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 *
 * It was originally written in Go programming language
 */
package main

import (
	"bufio"
	"os"
)

// Scanner handles reading MicroScript files
type Scanner struct {
	filePath string
}

// NewScanner creates a new Scanner instance
func NewScanner(filePath string) *Scanner {
	return &Scanner{
		filePath: filePath,
	}
}

// ReadLines reads all lines from the file and returns them as a slice of strings
func (s *Scanner) ReadLines() ([]string, error) {
	file, err := os.Open(s.filePath)
	if err != nil {
		return nil, err
	}
	defer file.Close()

	var lines []string
	scanner := bufio.NewScanner(file)

	for scanner.Scan() {
		lines = append(lines, scanner.Text())
	}

	if err := scanner.Err(); err != nil {
		return nil, err
	}

	return lines, nil
}
