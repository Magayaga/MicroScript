/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in C# programming language.
 */
using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;

namespace com.magayaga.microscript
{
    public class Parser
    {
        private readonly List<string> lines;
        private readonly Environment environment;

        public Parser(List<string> lines)
        {
            this.lines = lines;
            this.environment = new Environment();
        }

        public void Parse()
        {
            bool hasCStyleMain = false;
            int i = 0;
            while (i < lines.Count)
            {
                var line = lines[i].Trim();
                if (line.StartsWith("//") || line == string.Empty)
                {
                    i++;
                    continue;
                }

                if (Regex.IsMatch(line, @"^(String|Int32|Int64|Float32|Float64|fn)\s+\w+\s*\(.*\)\s*\{"))
                {
                    int closingBraceIndex = FindClosingBrace(i);
                    if (closingBraceIndex == -1)
                    {
                        throw new Exception("Syntax error: Unmatched '{' in function definition.");
                    }
                    ParseFunction(i, closingBraceIndex);
                    if (Regex.IsMatch(line, @"^fn\s+main\s*\("))
                    {
                        hasCStyleMain = true;
                    }
                    i = closingBraceIndex + 1;
                }
                else if (line.StartsWith("function "))
                {
                    int closingBraceIndex = FindClosingBrace(i);
                    if (closingBraceIndex == -1)
                    {
                        throw new Exception("Syntax error: Unmatched '{' in function definition.");
                    }
                    ParseFunction(i, closingBraceIndex);
                    i = closingBraceIndex + 1;
                }
                else if (line.StartsWith("if"))
                {
                    var executor = new Executor(environment);
                    i = Statements.ProcessConditionalStatement(lines, i, executor);
                }
                else if (line.StartsWith("for") || line.StartsWith("while"))
                {
                    var executor = new Executor(environment);
                    i = Statements.ProcessLoopStatement(lines, i, executor);
                }
                else
                {
                    ParseLine(line);
                    i++;
                }
            }

            if (hasCStyleMain)
            {
                var mainFunction = environment.GetFunction("main");
                if (mainFunction != null)
                {
                    var executor = new Executor(environment);
                    executor.ExecuteFunction("main", Array.Empty<string>());
                }
            }
        }

        private void ParseFunction(int start, int end)
        {
            var header = lines[start].Trim();
            var cStyleMatcher = Regex.Match(header, @"^(String|Int32|Int64|Float32|Float64|fn)\s+(\w+)\s*\(([^)]*)\)\s*\{");
            var microScriptMatcher = Regex.Match(header, @"function\s+(\w+)\(([^)]*)\)\s*(->\s*(\w+))?\s*\{");

            string name;
            string paramsStr;
            string returnType;

            if (cStyleMatcher.Success)
            {
                returnType = cStyleMatcher.Groups[1].Value.Trim();
                name = cStyleMatcher.Groups[2].Value;
                paramsStr = cStyleMatcher.Groups[3].Value.Trim();
            }
            else if (microScriptMatcher.Success)
            {
                name = microScriptMatcher.Groups[1].Value;
                paramsStr = microScriptMatcher.Groups[2].Value.Trim();
                returnType = microScriptMatcher.Groups[4].Success ? microScriptMatcher.Groups[4].Value.Trim() : "void";
            }
            else
            {
                throw new Exception("Syntax error: Invalid function declaration.");
            }

            var parameters = new List<Parameter>();
            if (!string.IsNullOrEmpty(paramsStr))
            {
                foreach (var param in paramsStr.Split(new[] { ',' }, StringSplitOptions.RemoveEmptyEntries))
                {
                    var parts = param.Split(':');
                    if (parts.Length != 2)
                    {
                        throw new Exception("Syntax error: Invalid parameter declaration.");
                    }
                    parameters.Add(new Parameter(parts[0].Trim(), parts[1].Trim()));
                }
            }
            var body = new List<string>();
            for (int i = start + 1; i < end; i++)
            {
                body.Add(lines[i].Trim());
            }

            environment.DefineFunction(new Function(name, parameters, returnType, body));
        }

        private int FindClosingBrace(int start)
        {
            int openBraces = 0;
            for (int i = start; i < lines.Count; i++)
            {
                var line = lines[i];
                openBraces += line.Split('{').Length - 1;
                openBraces -= line.Split('}').Length - 1;
                if (openBraces == 0)
                {
                    return i;
                }
            }
            return -1;
        }

        private void ParseLine(string line)
        {
            if (line.StartsWith("import "))
            {
                var moduleName = line.Substring(7).Trim();
                Import.ImportModule(moduleName, environment);
                return;
            }
            if (line.StartsWith("@map"))
            {
                var executor = new Executor(environment);
                ParseMapOperation(line, executor);
                return;
            }

            var pattern = new Regex(@"console.write\((.*)\);");
            var matcher = pattern.Match(line);
            if (matcher.Success)
            {
                var expression = matcher.Groups[1].Value;
                var executor = new Executor(environment);
                executor.Execute($"console.write({expression})");
                return;
            }

            var callPattern = new Regex(@"([\w:]+)\((.*)\);");
            var callMatcher = callPattern.Match(line);
            if (callMatcher.Success)
            {
                var functionName = callMatcher.Groups[1].Value;
                var args = callMatcher.Groups[2].Value.Trim();
                var executor = new Executor(environment);
                if (string.IsNullOrEmpty(args))
                {
                    executor.ExecuteFunction(functionName, new string[0]);
                }
                else
                {
                    executor.ExecuteFunction(functionName, args.Split(new[] { ',' }, StringSplitOptions.RemoveEmptyEntries));
                }
                return;
            }

            if (line.StartsWith("var ") || line.StartsWith("bool ") || line.StartsWith("list "))
            {
                var executor = new Executor(environment);
                executor.Execute(line);
                return;
            }

            if (line.Contains("="))
            {
                var equalsIndex = line.IndexOf('=');
                var varName = line.Substring(0, equalsIndex).Trim();
                var valueExpression = line.Substring(equalsIndex + 1).Trim().Replace(";", "");
                var executor = new Executor(environment);
                executor.Execute($"{varName} = {valueExpression}");
            }
        }

        private void ParseMapOperation(string line, Executor executor)
        {
            var matcher = Regex.Match(line, @"@map\s*=>\s*(\([^)]+\))\s*\[([^\]]+)\]");
            if (!matcher.Success)
            {
                throw new Exception($"Invalid @map syntax: {line}");
            }

            var operation = matcher.Groups[1].Value.Trim();
            var listExpression = matcher.Groups[2].Value.Trim();
            var list = new List<object>();
            foreach (var element in listExpression.Split(new[] { ',' }, StringSplitOptions.RemoveEmptyEntries))
            {
                list.Add(executor.Evaluate(element.Trim()));
            }

            var result = FunctionHigherOrder.ProcessMap(operation, list);
            environment.SetVariable("_last_map_result", result);
            Console.WriteLine($"Map result {operation}: [{string.Join(", ", result)}]");
        }

    }
}
