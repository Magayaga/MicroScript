/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * Converted from Java source.
 */
using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;

namespace com.magayaga.microscript
{
    public class Statements
    {
        public class BreakException : Exception { }
        public class ContinueException : Exception { }

        public static int ProcessConditionalStatement(List<string> lines, int startIndex, Executor executor)
        {
            int currentIndex = startIndex;
            var lineAndIndex = GetNonEmptyNonCommentLineWithIndex(lines, currentIndex);
            var line = lineAndIndex.line;
            currentIndex = lineAndIndex.index;

            if (line != null && line.StartsWith("if"))
            {
                var ifMatcher = Regex.Match(line, @"if\s*\((.+?)\)\s*(\{)?");
                if (!ifMatcher.Success)
                {
                    throw new Exception($"Invalid if statement syntax at line: {line}");
                }

                var condition = ifMatcher.Groups[1].Value.Trim();
                bool conditionValue = IsTrue(EvaluateCondition(condition, executor));

                int blockStartIndex = currentIndex;
                if (!ifMatcher.Groups[2].Success)
                {
                    blockStartIndex = FindNextOpeningBrace(lines, currentIndex + 1);
                    if (blockStartIndex == -1) throw new Exception($"Missing opening brace for if statement at or after line: {line}");
                }

                int blockEndIndex = FindMatchingClosingBrace(lines, blockStartIndex);
                if (blockEndIndex == -1) throw new Exception($"Missing closing brace for if statement starting at line: {line}");

                if (conditionValue)
                {
                    ExecuteBlock(lines, blockStartIndex + 1, blockEndIndex, executor);
                    return FindEndOfConditionalStructure(lines, blockEndIndex + 1);
                }

                currentIndex = blockEndIndex + 1;
                while (currentIndex < lines.Count)
                {
                    lineAndIndex = GetNonEmptyNonCommentLineWithIndex(lines, currentIndex);
                    line = lineAndIndex.line;
                    currentIndex = lineAndIndex.index;
                    if (line == null) break;

                    if (line.StartsWith("elif"))
                    {
                        var elifMatcher = Regex.Match(line, @"elif\s*\((.+?)\)\s*(\{)?");
                        if (!elifMatcher.Success) throw new Exception($"Invalid elif statement syntax at line: {line}");

                        bool elifValue = IsTrue(EvaluateCondition(elifMatcher.Groups[1].Value.Trim(), executor));
                        int elifBlockStartIndex = currentIndex;
                        if (!elifMatcher.Groups[2].Success)
                        {
                            elifBlockStartIndex = FindNextOpeningBrace(lines, currentIndex + 1);
                            if (elifBlockStartIndex == -1) throw new Exception($"Missing opening brace for elif statement at or after line: {line}");
                        }

                        int elifBlockEndIndex = FindMatchingClosingBrace(lines, elifBlockStartIndex);
                        if (elifBlockEndIndex == -1) throw new Exception($"Missing closing brace for elif statement at line: {line}");

                        if (elifValue)
                        {
                            ExecuteBlock(lines, elifBlockStartIndex + 1, elifBlockEndIndex, executor);
                            return FindEndOfConditionalStructure(lines, elifBlockEndIndex + 1);
                        }

                        currentIndex = elifBlockEndIndex + 1;
                    }
                    else if (line.StartsWith("else"))
                    {
                        int elseBlockStartIndex = currentIndex;
                        if (!line.Contains("{"))
                        {
                            elseBlockStartIndex = FindNextOpeningBrace(lines, currentIndex + 1);
                            if (elseBlockStartIndex == -1) throw new Exception($"Missing opening brace for else statement at or after line: {line}");
                        }

                        int elseBlockEndIndex = FindMatchingClosingBrace(lines, elseBlockStartIndex);
                        if (elseBlockEndIndex == -1) throw new Exception($"Missing closing brace for else statement at line: {line}");
                        ExecuteBlock(lines, elseBlockStartIndex + 1, elseBlockEndIndex, executor);
                        return elseBlockEndIndex + 1;
                    }
                    else
                    {
                        return currentIndex;
                    }
                }

                return currentIndex;
            }

            return startIndex + 1;
        }

        public static int ProcessLoopStatement(List<string> lines, int startIndex, Executor executor)
        {
            int currentIndex = startIndex;
            var lineAndIndex = GetNonEmptyNonCommentLineWithIndex(lines, currentIndex);
            var line = lineAndIndex.line;
            currentIndex = lineAndIndex.index;

            if (line != null && (line.StartsWith("for") || line.StartsWith("while")))
            {
                int blockStartIndex = currentIndex;
                if (!line.Contains("{"))
                {
                    blockStartIndex = FindNextOpeningBrace(lines, currentIndex + 1);
                    if (blockStartIndex == -1) throw new Exception($"Missing opening brace for loop statement at or after line: {line}");
                }

                int blockEndIndex = FindMatchingClosingBrace(lines, blockStartIndex);
                if (blockEndIndex == -1) throw new Exception($"Missing closing brace for loop statement starting at line: {line}");

                ExecuteLoopBlock(lines, blockStartIndex + 1, blockEndIndex, executor, line);
                return blockEndIndex + 1;
            }

            return startIndex + 1;
        }

        private static object EvaluateCondition(string condition, Executor executor)
        {
            var evaluator = new ExpressionEvaluator(condition, executor.GetEnvironment());
            return evaluator.Parse();
        }

        private static void ExecuteLoopBlock(List<string> lines, int startIndex, int endIndex, Executor executor, string loopDeclaration)
        {
            if (loopDeclaration.StartsWith("while"))
            {
                var m = Regex.Match(loopDeclaration, @"while\s*\((.+?)\)");
                if (!m.Success) throw new Exception($"Invalid while loop syntax: {loopDeclaration}");
                var condition = m.Groups[1].Value.Trim();
                while (IsTrue(EvaluateCondition(condition, executor)))
                {
                    try { ExecuteBlock(lines, startIndex, endIndex, executor); }
                    catch (BreakException) { break; }
                    catch (ContinueException) { continue; }
                }
            }
            else if (loopDeclaration.StartsWith("for"))
            {
                var m = Regex.Match(loopDeclaration, @"for\s*\((.+?)\)");
                if (!m.Success) throw new Exception($"Invalid for loop syntax: {loopDeclaration}");
                var parts = m.Groups[1].Value.Trim().Split(';');
                if (parts.Length != 3) throw new Exception("For loop must have format: for (init; condition; increment)");

                var initialization = parts[0].Trim();
                var condition = parts[1].Trim();
                var increment = parts[2].Trim();
                if (!string.IsNullOrEmpty(initialization)) executor.Execute(initialization);

                while (string.IsNullOrEmpty(condition) || IsTrue(EvaluateCondition(condition, executor)))
                {
                    try { ExecuteBlock(lines, startIndex, endIndex, executor); }
                    catch (BreakException) { break; }
                    catch (ContinueException)
                    {
                        if (!string.IsNullOrEmpty(increment)) executor.Execute(increment);
                        continue;
                    }

                    if (!string.IsNullOrEmpty(increment)) executor.Execute(increment);
                }
            }
        }

        private static int FindMatchingClosingBrace(List<string> lines, int openingBraceLineIndex)
        {
            int braceCount = 0;
            var firstLine = lines[openingBraceLineIndex].Trim();
            int openingBracePos = firstLine.IndexOf('{');
            if (openingBracePos == -1) throw new Exception($"No opening brace found at line index: {openingBraceLineIndex}");

            braceCount = 1;
            for (int i = openingBracePos + 1; i < firstLine.Length; i++)
            {
                if (firstLine[i] == '{') braceCount++;
                else if (firstLine[i] == '}')
                {
                    braceCount--;
                    if (braceCount == 0) return openingBraceLineIndex;
                }
            }

            for (int i = openingBraceLineIndex + 1; i < lines.Count; i++)
            {
                var line = lines[i];
                if (line.Trim().StartsWith("//")) continue;
                for (int j = 0; j < line.Length; j++)
                {
                    if (line[j] == '{') braceCount++;
                    else if (line[j] == '}')
                    {
                        braceCount--;
                        if (braceCount == 0) return i;
                    }
                }
            }
            return -1;
        }

        private static void ExecuteBlock(List<string> lines, int startIndex, int endIndex, Executor executor)
        {
            for (int i = startIndex; i < endIndex; i++)
            {
                var line = lines[i].Trim();
                if (string.IsNullOrEmpty(line) || line == "}" || line.StartsWith("//")) continue;
                if (line == "break;" || line == "break") throw new BreakException();
                if (line == "continue;" || line == "continue") throw new ContinueException();

                if (line.StartsWith("if")) { i = ProcessConditionalStatement(lines, i, executor) - 1; continue; }
                if (line.StartsWith("for") || line.StartsWith("while")) { i = ProcessLoopStatement(lines, i, executor) - 1; continue; }

                executor.Execute(line);
            }
        }

        private static int FindEndOfConditionalStructure(List<string> lines, int startIndex)
        {
            int currentIndex = startIndex;
            while (currentIndex < lines.Count)
            {
                var lineAndIndex = GetNonEmptyNonCommentLineWithIndex(lines, currentIndex);
                if (lineAndIndex.line == null) break;

                var line = lineAndIndex.line;
                currentIndex = lineAndIndex.index;

                if (line.StartsWith("elif") || (line.StartsWith("else") && (line.Contains("{") || !line.Contains(";"))))
                {
                    int blockStartIndex = line.Contains("{") ? currentIndex : FindNextOpeningBrace(lines, currentIndex + 1);
                    if (blockStartIndex == -1) throw new Exception($"Missing opening brace for elif/else at line: {line}");
                    int blockEndIndex = FindMatchingClosingBrace(lines, blockStartIndex);
                    if (blockEndIndex == -1) throw new Exception($"Missing closing brace for elif/else at line: {line}");
                    currentIndex = blockEndIndex + 1;
                }
                else
                {
                    return currentIndex;
                }
            }
            return currentIndex;
        }

        private static bool IsTrue(object value)
        {
            if (value == null) return false;
            if (value is bool b) return b;
            if (value is int i) return i != 0;
            if (value is long l) return l != 0;
            if (value is float f) return Math.Abs(f) > 0.0001f;
            if (value is double d) return Math.Abs(d) > 0.0001;
            if (value is string s) return !string.IsNullOrEmpty(s);
            return true;
        }

        private static (string line, int index) GetNonEmptyNonCommentLineWithIndex(List<string> lines, int index)
        {
            while (index < lines.Count)
            {
                var line = lines[index].Trim();
                if (!string.IsNullOrEmpty(line) && !line.StartsWith("//")) return (line, index);
                index++;
            }
            return (null, -1);
        }

        private static int FindNextOpeningBrace(List<string> lines, int startIndex)
        {
            for (int i = startIndex; i < lines.Count; i++)
            {
                var line = lines[i].Trim();
                if (!string.IsNullOrEmpty(line) && !line.StartsWith("//") && line.Contains("{")) return i;
            }
            return -1;
        }
    }
}
