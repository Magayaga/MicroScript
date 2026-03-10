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
    public class Define
    {
        private sealed class MacroDef
        {
            public List<string> Params { get; }
            public string Body { get; }
            public MacroDef(List<string> @params, string body)
            {
                Params = @params;
                Body = body;
            }
        }

        private readonly Dictionary<string, string> objectMacros = new Dictionary<string, string>();
        private readonly Dictionary<string, MacroDef> functionMacros = new Dictionary<string, MacroDef>();

        public List<string> Preprocess(List<string> lines)
        {
            var output = new List<string>();
            foreach (var line in lines)
            {
                var trimmed = line.Trim();
                if (trimmed.StartsWith("#define")) ParseDefine(trimmed);
                else if (trimmed.StartsWith("#undef")) ParseUndef(trimmed);
                else output.Add(ExpandMacros(line));
            }
            return output;
        }

        private void ParseDefine(string line)
        {
            var funcMatch = Regex.Match(line, @"#define\s+([A-Z_][A-Z0-9_]*)\s*\(([^)]*)\)\s*(.*)");
            if (funcMatch.Success)
            {
                var name = funcMatch.Groups[1].Value;
                var paramList = funcMatch.Groups[2].Value.Trim();
                var parameters = string.IsNullOrEmpty(paramList) ? new List<string>() : new List<string>(Regex.Split(paramList, @"\s*,\s*"));
                var body = funcMatch.Groups[3].Value.Trim();
                functionMacros[name] = new MacroDef(parameters, body);
                return;
            }

            var objMatch = Regex.Match(line, @"#define\s+([A-Z_][A-Z0-9_]*)(?:\s+(.*))?");
            if (objMatch.Success)
            {
                var name = objMatch.Groups[1].Value;
                var value = objMatch.Groups[2].Success ? objMatch.Groups[2].Value.Trim() : string.Empty;
                objectMacros[name] = value;
            }
        }

        private void ParseUndef(string line)
        {
            var undefMatch = Regex.Match(line, @"#undef\s+([A-Z_][A-Z0-9_]*)");
            if (!undefMatch.Success) return;
            var name = undefMatch.Groups[1].Value;
            objectMacros.Remove(name);
            functionMacros.Remove(name);
        }

        public string ExpandMacros(string line)
        {
            var result = line;
            for (int pass = 0; pass < 10; pass++)
            {
                var before = result;
                result = ExpandFunctionMacros(result);
                result = ExpandObjectMacros(result);
                if (before == result) break;
            }
            return result;
        }

        private string ExpandObjectMacros(string line)
        {
            var result = line;
            foreach (var macro in objectMacros)
            {
                result = Regex.Replace(result, $@"\b{Regex.Escape(macro.Key)}\b", macro.Value);
            }
            return result;
        }

        private string ExpandFunctionMacros(string line)
        {
            var result = line;
            bool replaced;
            do
            {
                replaced = false;
                foreach (var entry in functionMacros)
                {
                    var name = entry.Key;
                    var macro = entry.Value;
                    var pattern = new Regex($@"\b{Regex.Escape(name)}\s*\(([^()]*(?:\([^()]*\)[^()]*)*)\)");
                    var match = pattern.Match(result);
                    if (!match.Success) continue;

                    var args = SplitArgs(match.Groups[1].Value);
                    if (args.Count != macro.Params.Count)
                    {
                        result = pattern.Replace(result, $"/*MACRO_ARG_ERROR:{name}*/", 1);
                    }
                    else
                    {
                        var body = macro.Body;
                        for (int i = 0; i < macro.Params.Count; i++)
                        {
                            body = Regex.Replace(body, $@"\b{Regex.Escape(macro.Params[i].Trim())}\b", args[i].Trim());
                        }
                        result = pattern.Replace(result, body, 1);
                    }

                    replaced = true;
                    break;
                }
            } while (replaced);
            return result;
        }

        private static List<string> SplitArgs(string input)
        {
            var args = new List<string>();
            int depth = 0;
            int start = 0;
            for (int i = 0; i < input.Length; i++)
            {
                if (input[i] == '(') depth++;
                else if (input[i] == ')') depth--;
                else if (input[i] == ',' && depth == 0)
                {
                    args.Add(input.Substring(start, i - start));
                    start = i + 1;
                }
            }
            if (start <= input.Length) args.Add(input.Substring(start));
            return args;
        }
    }
}
