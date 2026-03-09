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

                        environment.SetVariable(varName, CoerceTypedValue(typeAnnotation, value, valueExpression));
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
                localEnv.SetVariable(parameters[i].GetName(), CoerceTypedValue(expectedType, value, $"Argument {args[i]}"));
            }

            object? returnValue = null;
            foreach (var line in function.GetBody())
            {
                if (line.Trim().StartsWith("return"))
                {
                    var returnExpression = line.Substring(line.IndexOf("return") + 6).Trim().Replace(";", "");
                    returnValue = new Executor(localEnv).Evaluate(returnExpression);
                    var expectedReturnType = function.GetReturnType();
                    return CoerceTypedValue(expectedReturnType, returnValue, $"Return value {returnValue}");
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

        private static object CoerceTypedValue(string typeAnnotation, object? value, string subject)
        {
            switch (typeAnnotation)
            {
                case "String":
                    if (value is string str)
                    {
                        return str;
                    }
                    throw new Exception($"Type error: {subject} is not a String.");

                case "Int32":
                    return CoerceInteger(value, subject, int.MinValue, int.MaxValue, "Int32", n => (int)n);

                case "Int64":
                    return CoerceInteger(value, subject, long.MinValue, long.MaxValue, "Int64", n => n);

                case "Float32":
                    if (value is IConvertible conv32)
                    {
                        return Convert.ToSingle(conv32);
                    }
                    throw new Exception($"Type error: {subject} is not a Float32.");

                case "Float64":
                    if (value is IConvertible conv64)
                    {
                        return Convert.ToDouble(conv64);
                    }
                    throw new Exception($"Type error: {subject} is not a Float64.");

                default:
                    throw new Exception($"Unknown type annotation: {typeAnnotation}");
            }
        }

        private static object CoerceInteger(object? value, string subject, long min, long max, string typeName, Func<long, object> converter)
        {
            if (!(value is IConvertible conv))
            {
                throw new Exception($"Type error: {subject} is not an {typeName}.");
            }

            var number = Convert.ToDouble(conv);
            if (Math.Abs(number % 1) > 0.0000001)
            {
                throw new Exception($"Type error: {subject} is not an {typeName}.");
            }

            var integerValue = Convert.ToInt64(number);
            if (integerValue < min || integerValue > max)
            {
                throw new Exception($"Type error: {subject} is out of range for {typeName}.");
            }

            return converter(integerValue);
        }
    }
}
