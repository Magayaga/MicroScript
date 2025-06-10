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
	"path/filepath"
)

// Valid file extensions for MicroScript files
var validExtensions = map[string]bool{
	".microscript": true,
	".mus":         true,
	".micros":      true,
}

const runCommand = "run"

func main() {
	args := os.Args[1:] // Remove program name from args

	// Handle CLI commands early return pattern
	if shouldDelegateToCli(args) {
		handleCli(args)
		return
	}

	if !isValidRunCommand(args) {
		printUsage()
		return
	}

	filePath := args[1]

	// Validate file extension
	if !hasValidExtension(filePath) {
		printExtensionError(filePath)
		return
	}

	// Execute MicroScript file
	executeScript(filePath)
}

// shouldDelegateToCli determines if the command should be delegated to CLI handler
func shouldDelegateToCli(args []string) bool {
	if len(args) == 0 {
		return true
	}

	firstArg := args[0]
	return firstArg == "--help" || firstArg == "--version" || firstArg == "about"
}

// isValidRunCommand validates if the command is a proper run command with required arguments
func isValidRunCommand(args []string) bool {
	return len(args) >= 2 && args[0] == runCommand
}

// hasValidExtension efficiently checks if file has valid MicroScript extension using map lookup
// Time complexity: O(1) average case
func hasValidExtension(filePath string) bool {
	ext := filepath.Ext(filePath)
	if ext == "" {
		return false
	}
	return validExtensions[ext]
}

// printExtensionError prints formatted error message for invalid file extensions
func printExtensionError(filePath string) {
	fmt.Fprintf(os.Stderr, "Error: File must have a valid MicroScript extension (.microscript, .mus, .micros)\n")
	fmt.Fprintf(os.Stderr, "The file '%s' does not have a recognized MicroScript extension.\n", filePath)
}

// executeScript executes the MicroScript file with proper error handling
func executeScript(filePath string) {
	// Create Scanner object
	scanner := NewScanner(filePath)

	// Read and preprocess lines
	lines, err := scanner.ReadLines()
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error reading file '%s': %v\n", filePath, err)
		return
	}

	// Preprocess macros
	define := NewDefine()
	preprocessedLines := define.Preprocess(lines)

	// Parse and execute
	parser := NewParser(preprocessedLines)
	err = parser.Parse()
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error executing script '%s': %v\n", filePath, err)
		return
	}
}

// CLI handling functions
const (
	reset  = "\033[0m"
	green  = "\033[32;1m"       // Bold green
	blue   = "\033[34;1m"       // Bold blue
	orange = "\033[38;5;208;1m" // Bold orange
)

const (
	version = "MicroScript v0.2.0" // Updated version for Go implementation
	author  = "Cyril John Magayaga"
)

func printUsage() {
	fmt.Printf("%sUsage:%s %smicroscript <command> [options]%s\n", green, reset, blue, reset)
	fmt.Printf("%sOptions:%s\n", green, reset)
	fmt.Printf("  %s--help%s        Show help information\n", blue, reset)
	fmt.Printf("  %s--version%s     Show version information\n", blue, reset)
	fmt.Printf("%sCommands:%s\n", green, reset)
	fmt.Printf("  %srun%s           Run a MicroScript source file\n", blue, reset)
	fmt.Printf("  %sabout%s         Show about information\n", blue, reset)
	fmt.Printf("\n%sSupported file extensions:%s .microscript, .mus, .micros\n", green, reset)
}

func printHelp() {
	printUsage()
	fmt.Println()
	url := "https://github.com/magayaga/microscript"
	fmt.Printf("For more information, visit: %s%s%s\n", orange, url, reset)
	fmt.Printf("\n%sExamples:%s\n", green, reset)
	fmt.Printf("  %smicroscript run hello.microscript%s\n", blue, reset)
	fmt.Printf("  %smicroscript run program.mus%s\n", blue, reset)
	fmt.Printf("  %smicroscript --version%s\n", blue, reset)
}

func printVersion() {
	fmt.Printf("%s%s%s\n", blue, version, reset)
	fmt.Printf("Go implementation - High performance MicroScript interpreter\n")
}

func printAbout() {
	fmt.Printf("%sMicroScript - The programming language%s\n", blue, reset)
	fmt.Printf("High-performance Go implementation\n")
	fmt.Printf("Copyright (c) 2024-2025 %s%s%s\n", green, author, reset)
	fmt.Printf("\n%sFeatures:%s\n", green, reset)
	fmt.Printf("  • Fast native compilation\n")
	fmt.Printf("  • Macro preprocessing with #define/#undef\n")
	fmt.Printf("  • C-style and MicroScript function syntax\n")
	fmt.Printf("  • Arrow functions and lambda expressions\n")
	fmt.Printf("  • Comprehensive error handling\n")
}

func handleCli(args []string) {
	if len(args) == 0 || args[0] == "--help" {
		printHelp()
		return
	}

	switch args[0] {
	case "--version":
		printVersion()
	case "about":
		printAbout()
	case "run":
		if len(args) < 2 {
			fmt.Printf("%sError:%s Missing file path for run command\n", "\033[31;1m", reset)
			printUsage()
			return
		}

		filePath := args[1]
		if !hasValidExtension(filePath) {
			printExtensionError(filePath)
			return
		}

		fmt.Printf("%sRunning MicroScript file:%s %s\n", blue, reset, filePath)
		executeScript(filePath)
	default:
		fmt.Printf("%sError:%s Unknown command: %s\n", "\033[31;1m", reset, args[0])
		printUsage()
	}
}
