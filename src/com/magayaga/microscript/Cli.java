/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

public class Cli {
    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32;1m"; // Bold green
    private static final String BLUE = "\u001B[34;1m";  // Bold blue

    private static final String VERSION = "MicroScript 0.1.0";
    private static final String AUTHOR = "Cyril John Magayaga";

    public static void printUsage() {
        System.out.println(GREEN + "Usage:" + RESET + " " + BLUE + "microscript <command> [options]" + RESET);
        System.out.println(GREEN + "Options:" + RESET);
        System.out.println("  " + BLUE + "--help" + RESET + "        Show help information");
        System.out.println("  " + BLUE + "--version" + RESET + "     Show version information");
        System.out.println(GREEN + "Commands:" + RESET);
        System.out.println("  " + BLUE + "run" + RESET + "           Run a MicroScript file");
        System.out.println("  " + BLUE + "about" + RESET + "         Show about information");
    }

    public static void printHelp() {
        printUsage();
        System.out.println();
        // ANSI orange (bright yellow) and black for https
        final String ORANGE = "\u001B[38;5;208;1m"; // Bold orange (if supported)
        String url = "https://github.com/magayaga/microscript";
        System.out.println("For more information, visit: " + ORANGE + url + RESET);
    }

    public static void printVersion() {
        System.out.println(BLUE + VERSION + RESET);
    }

    public static void printAbout() {
        System.out.println(BLUE + "MicroScript - The programming language" + RESET);
        System.out.println("Copyright (c) 2024-2025 " + GREEN + "Cyril John Magayaga" + RESET);
    }

    // Example entry point for CLI testing
    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("--help")) {
            printHelp();
        } else if (args[0].equals("--version")) {
            printVersion();
        } else if (args[0].equals("about")) {
            printAbout();
        } else if (args[0].equals("run")) {
            System.out.println(BLUE + "Running MicroScript file..." + RESET);
            // You can delegate to MicroScript.main(args) here if needed
        } else {
            System.out.println("Unknown command: " + args[0]);
            printUsage();
        }
    }
}