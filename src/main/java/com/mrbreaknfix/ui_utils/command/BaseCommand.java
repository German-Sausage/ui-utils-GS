/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file BaseCommand.java
 */
package com.mrbreaknfix.ui_utils.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseCommand implements Command {

    protected abstract CommandResult<?> executeParsed(List<Object> parsedArgs);

    @Override
    public CommandResult<?> execute(List<String> args) {
        try {
            List<Object> parsedArgs = parseArguments(args);
            return executeParsed(parsedArgs);
        } catch (IllegalArgumentException e) {
            return CommandResult.of(false, "Invalid arguments: " + e.getMessage());
        }
    }

    @Override
    public boolean visible() {
        return true;
    }

    public abstract String manual();

    protected ParsedInput parseNamedArguments(List<String> args) {
        List<String> positional = new ArrayList<>();
        Map<String, String> named = new HashMap<>();

        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            if (arg.startsWith("--")) {
                String key = arg.substring(2);
                if (i + 1 < args.size() && !args.get(i + 1).startsWith("--")) {
                    named.put(key, args.get(i + 1));
                    i++; // Skip the next argument since it's a value
                } else {
                    named.put(key, "true"); // A flag without a value
                }
            } else {
                positional.add(arg);
            }
        }
        return new ParsedInput(positional, named);
    }

    protected List<Object> parseArguments(List<String> args) {
        String input = String.join(" ", args);
        List<Object> parsedArgs = new ArrayList<>();
        if (input == null || input.trim().isEmpty()) {
            return parsedArgs;
        }

        StringBuilder currentArg = new StringBuilder();
        char activeQuoteChar = 0; // The character for the current quote (' or "), or 0 if none.
        boolean inArgument = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (activeQuoteChar != 0) { // We are inside a quoted segment
                if (c == activeQuoteChar) {
                    activeQuoteChar = 0; // Exit quoted mode
                    // The argument is finalized by whitespace, or end of string
                } else {
                    currentArg.append(c);
                }
            } else { // We are outside a quoted segment
                if (Character.isWhitespace(c)) {
                    if (inArgument) {
                        parsedArgs.add(currentArg.toString());
                        currentArg.setLength(0);
                        inArgument = false;
                    }
                } else {
                    if (!inArgument) {
                        inArgument = true;
                    }
                    if (c == '\'' || c == '"') {
                        activeQuoteChar = c; // Enter quoted mode
                    } else {
                        currentArg.append(c);
                    }
                }
            }
        }

        if (inArgument) {
            parsedArgs.add(currentArg.toString());
        }

        return parsedArgs;
    }

    // hold the results of the new parser
    protected record ParsedInput(List<String> positional, Map<String, String> named) {}
}
