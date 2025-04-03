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
            int i = 0;
            while (i < lines.Count)
            {
                var line = lines[i].Trim();
                if (line.StartsWith("//") || line == string.Empty)
                {
                    i++;
                    continue;
                }

                if (line.StartsWith("function "))
                {
                    int closingBraceIndex = FindClosingBrace(i);
                    if (closingBraceIndex == -1)
                    {
                        throw new Exception("Syntax error: Unmatched '{' in function definition.");
                    }
                    ParseFunction(i, closingBraceIndex);
                    i = closingBraceIndex + 1;
                }
                else
                {
                    ParseLine(line);
                    i++;
                }
            }
        }

        private void ParseFunction(int start, int end)
        {
            var header = lines[start].Trim();
            var matcher = Regex.Match(header, @"function\s+(\w+)\(([^)]*)\)\s*(->\s*(\w+))?\s*\{");
            if (!matcher.Success)
            {
                throw new Exception("Syntax error: Invalid function declaration.");
            }

            var name = matcher.Groups[1].Value;
            var paramsStr = matcher.Groups[2].Value.Trim();
            var returnType = matcher.Groups[4].Success ? matcher.Groups[4].Value.Trim() : "void";
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
            var pattern = new Regex(@"console.write\((.*)\);");
            var matcher = pattern.Match(line);
            if (matcher.Success)
            {
                var expression = matcher.Groups[1].Value;
                var executor = new Executor(environment);
                executor.Execute($"console.write({expression})");
                return;
            }

            var callPattern = new Regex(@"(\w+)\((.*)\);");
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

            if (line.StartsWith("var ") || line.StartsWith("bool "))
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
    }
}