/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in C# programming language.
 */
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Text.RegularExpressions;

namespace com.magayaga.microscript
{
    public class Executor
    {
        private readonly Environment environment;

        public Executor(Environment environment)
        {
            this.environment = environment ?? throw new ArgumentNullException(nameof(environment));
        }

        public void Execute(string expression)
        {
            try
            {
                if (expression.StartsWith("//"))
                {
                    return;
                }

                if (expression.StartsWith("console.write"))
                {
                    var pattern = new Regex(@"console.write\((.*)\);");
                    var matcher = pattern.Match(expression);
                    if (matcher.Success)
                    {
                        var innerExpression = matcher.Groups[1].Value.Trim();
                        var result = Evaluate(innerExpression);
                        Console.WriteLine(result);
                    }
                }

                else if (expression.StartsWith("console.system"))
                {
                    var pattern = new Regex(@"console.system\((.*)\);");
                    var matcher = pattern.Match(expression);
                    if (matcher.Success)
                    {
                        var command = matcher.Groups[1].Value.Trim();
                        ExecuteSystemCommand(command);
                    }
                }

                else if (expression.StartsWith("var "))
                {
                    var declaration = expression.Substring(4).Trim();
                    var equalsIndex = declaration.IndexOf('=');
                    if (equalsIndex != -1)
                    {
                        var varDeclaration = declaration.Substring(0, equalsIndex).Trim();
                        var parts = varDeclaration.Split(':');
                        if (parts.Length != 2)
                        {
                            throw new Exception($"Syntax error in variable declaration: {expression}");
                        }
                        var varName = parts[0].Trim();
                        var typeAnnotation = parts[1].Trim();
                        var valueExpression = declaration.Substring(equalsIndex + 1).Trim().Replace(";", "");
                        var value = Evaluate(valueExpression);

                        switch (typeAnnotation)
                        {
                            case "String":
                                if (!(value is string))
                                {
                                    throw new Exception($"Type error: {valueExpression} is not a String.");
                                }
                                break;
                            case "Int32":
                            case "Int64":
                                if (!(value is int))
                                {
                                    throw new Exception($"Type error: {valueExpression} is not an Integer.");
                                }
                                break;
                            case "Float32":
                                if (!(value is float))
                                {
                                    throw new Exception($"Type error: {valueExpression} is not a Float32.");
                                }
                                break;
                            case "Float64":
                                if (!(value is double))
                                {
                                    throw new Exception($"Type error: {valueExpression} is not a Float64.");
                                }
                                break;
                            default:
                                throw new Exception($"Unknown type annotation: {typeAnnotation}");
                        }

                        environment.SetVariable(varName, value);
                    }

                    else
                    {
                        throw new Exception($"Syntax error in variable declaration: {expression}");
                    }
                }

                else if (expression.StartsWith("bool "))
                {
                    var declaration = expression.Substring(5).Trim();
                    var equalsIndex = declaration.IndexOf('=');
                    if (equalsIndex != -1)
                    {
                        var boolName = declaration.Substring(0, equalsIndex).Trim();
                        var valueExpression = declaration.Substring(equalsIndex + 1).Trim().Replace(";", "");
                        var value = Evaluate(valueExpression);
                        if (value is bool)
                        {
                            environment.SetVariable(boolName, value);
                        }

                        else
                        {
                            throw new Exception($"Syntax error: {valueExpression} is not a boolean.");
                        }
                    }

                    else
                    {
                        throw new Exception($"Syntax error in boolean declaration: {expression}");
                    }
                }

                else if (expression.StartsWith("list "))
                {
                    var declaration = expression.Substring(5).Trim();
                    var equalsIndex = declaration.IndexOf('=');
                    if (equalsIndex != -1)
                    {
                        var listName = declaration.Substring(0, equalsIndex).Trim();
                        var valueExpression = declaration.Substring(equalsIndex + 1).Trim().Replace(";", "");
                        if (valueExpression.StartsWith("[") && valueExpression.EndsWith("]"))
                        {
                            var elements = valueExpression.Substring(1, valueExpression.Length - 2);
                            var list = new ListVariable(elements.Split(new[] { ',' }, StringSplitOptions.RemoveEmptyEntries));
                            environment.SetVariable(listName, list);
                        }

                        else
                        {
                            throw new Exception($"Syntax error in list declaration: {valueExpression}");
                        }
                    }

                    else
                    {
                        throw new Exception($"Syntax error in list declaration: {expression}");
                    }
                }

                else
                {
                    Evaluate(expression);
                }
            }

            catch (Exception e)
            {
                Console.WriteLine($"Evaluation error: {e.Message}");
            }
        }

        private void ExecuteSystemCommand(string command)
        {
            var cmdArray = command.Split(' ');
            var process = new Process
            {
                StartInfo = new ProcessStartInfo
                {
                    FileName = cmdArray[0],
                    Arguments = string.Join(" ", cmdArray, 1, cmdArray.Length - 1),
                    RedirectStandardOutput = true,
                    UseShellExecute = false,
                    CreateNoWindow = true
                }
            };
            process.Start();
            using (var reader = process.StandardOutput)
            {
                string? line;
                while ((line = reader.ReadLine()) != null)
                {
                    Console.WriteLine(line);
                }
            }
            process.WaitForExit();
        }

        public object? ExecuteFunction(string functionName, string[]? args)
        {
            var function = environment.GetFunction(functionName);
            if (function == null)
            {
                throw new Exception($"Function not found: {functionName}");
            }

            var parameters = function.GetParameters();
            args ??= Array.Empty<string>();
            if (parameters.Count != args.Length)
            {
                throw new Exception($"Argument count mismatch for function: {functionName}");
            }

            var localEnv = new Environment(environment);
            for (int i = 0; i < args.Length; i++)
            {
                var value = Evaluate(args[i]);
                var expectedType = parameters[i].GetType();
                switch (expectedType)
                {
                    case "String":
                        if (!(value is string))
                        {
                            throw new Exception($"Type error: Argument {args[i]} is not a String.");
                        }
                        break;
                    case "Int32":
                    case "Int64":
                        if (!(value is int))
                        {
                            throw new Exception($"Type error: Argument {args[i]} is not an Integer.");
                        }
                        break;
                    case "Float32":
                        if (!(value is float))
                        {
                            throw new Exception($"Type error: Argument {args[i]} is not a Float32.");
                        }
                        break;
                    case "Float64":
                        if (!(value is double))
                        {
                            throw new Exception($"Type error: Argument {args[i]} is not a Float64.");
                        }
                        break;
                    default:
                        throw new Exception($"Unknown type annotation: {expectedType}");
                }
                localEnv.SetVariable(parameters[i].GetName(), value);
            }

            object? returnValue = null;
            foreach (var line in function.GetBody())
            {
                if (line.Trim().StartsWith("return"))
                {
                    var returnExpression = line.Substring(line.IndexOf("return") + 6).Trim().Replace(";", "");
                    returnValue = new Executor(localEnv).Evaluate(returnExpression);
                    var expectedReturnType = function.GetReturnType();
                    switch (expectedReturnType)
                    {
                        case "String":
                            if (!(returnValue is string))
                            {
                                throw new Exception($"Type error: Return value {returnValue} is not a String.");
                            }
                            break;
                        case "Int32":
                        case "Int64":
                            if (!(returnValue is int))
                            {
                                throw new Exception($"Type error: Return value {returnValue} is not an Integer.");
                            }
                            break;
                        case "Float32":
                            if (!(returnValue is float))
                            {
                                throw new Exception($"Type error: Return value {returnValue} is not a Float32.");
                            }
                            break;
                        case "Float64":
                            if (!(returnValue is double))
                            {
                                throw new Exception($"Type error: Return value {returnValue} is not a Float64.");
                            }
                            break;
                        default:
                            throw new Exception($"Unknown return type annotation: {expectedReturnType}");
                    }
                    return returnValue;
                }
                new Executor(localEnv).Execute(line);
            }

            return returnValue;
        }

        public object Evaluate(string expression)
        {
            if (expression.StartsWith("\"") && expression.EndsWith("\""))
            {
                var strExpression = expression.Substring(1, expression.Length - 2);
                return InterpolateString(strExpression);
            }

            var functionCallPattern = new Regex(@"(\w+)\((.*)\)");
            var matcher = functionCallPattern.Match(expression);
            if (matcher.Success)
            {
                var functionName = matcher.Groups[1].Value;
                var args = matcher.Groups[2].Value.Trim();
                var arguments = string.IsNullOrEmpty(args) ? Array.Empty<string>() : args.Split(new[] { ',' }, StringSplitOptions.RemoveEmptyEntries);
                return ExecuteFunction(functionName, arguments) ?? throw new Exception($"Function '{functionName}' returned null.");
            }

            var variableValue = environment.GetVariable(expression);
            if (variableValue != null)
            {
                return variableValue;
            }

            if (expression == "true")
            {
                return true;
            }

            else if (expression == "false")
            {
                return false;
            }

            else if (expression.StartsWith("not "))
            {
                var value = Evaluate(expression.Substring(4).Trim());
                if (value is bool)
                {
                    return !(bool)value;
                }

                else
                {
                    throw new Exception($"Syntax error: {expression} is not a boolean.");
                }
            }

            else if (expression.StartsWith("!"))
            {
                var value = Evaluate(expression.Substring(1).Trim());
                if (value is bool)
                {
                    return !(bool)value;
                }

                else
                {
                    throw new Exception($"Syntax error: {expression} is not a boolean.");
                }
            }

            var evaluator = new ExpressionEvaluator(expression, environment);
            return evaluator.Parse();
        }

        private string InterpolateString(string strExpression)
        {
            var pattern = new Regex(@"\{(\w+)\}");
            var matches = pattern.Matches(strExpression);
            foreach (Match match in matches)
            {
                var varName = match.Groups[1].Value;
                var variableValue = environment.GetVariable(varName);
                if (variableValue != null)
                {
                    strExpression = strExpression.Replace($"{{{varName}}}", variableValue.ToString());
                }
                else
                {
                    throw new Exception($"Variable '{varName}' not found.");
                }
            }
            return strExpression;
        }
    }
}