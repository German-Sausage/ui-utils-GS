/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file MathCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.List;
import java.util.Stack;

import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;

public class MathCommand extends BaseCommand {
    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        if (parsedArgs.isEmpty()) {
            return CommandResult.of(false, "Usage: math <expression>");
        }

        String expr =
                String.join(
                                "",
                                parsedArgs.stream()
                                        .map(Object::toString)
                                        .toArray(CharSequence[]::new))
                        .replaceAll("\\s+", ""); // remove any spaces

        try {
            double result = evaluateExpression(expr);
            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("expression", expr);
            jsonBody.addProperty("result", result);
            return CommandResult.of(true, "Result: " + result, result, jsonBody);
        } catch (Exception e) {
            return CommandResult.of(false, "Error evaluating expression: " + e.getMessage());
        }
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal("math", "Evaluate a mathematical expression.")
                .then(
                        ArgumentNode.argument(
                                "<expression...>", "The expression to evaluate. e.g., (5+3)*2"));
    }

    /** Pre-processes the expression to add implicit multiplication operators. */
    private String addImplicitMultiplication(String expr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < expr.length(); i++) {
            char currentChar = expr.charAt(i);
            sb.append(currentChar);

            if (i < expr.length() - 1) {
                char nextChar = expr.charAt(i + 1);
                // Case: Digit followed by '(' -> e.g., 3(4+5) becomes 3*(4+5)
                if (Character.isDigit(currentChar) && nextChar == '(') {
                    sb.append('*');
                }
                // Case: ')' followed by a digit -> e.g., (5+3)2 becomes (5+3)*2
                else if (currentChar == ')' && Character.isDigit(nextChar)) {
                    sb.append('*');
                }
                // Case: ')' followed by '(' -> e.g., (5+3)(2+1) becomes (5+3)*(2+1)
                else if (currentChar == ')' && nextChar == '(') {
                    sb.append('*');
                }
            }
        }
        return sb.toString();
    }

    private double evaluateExpression(String expression) {
        // First, add implicit multiplication operators
        expression = addImplicitMultiplication(expression);

        Stack<Double> values = new Stack<>();
        Stack<Character> ops = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char currentChar = expression.charAt(i);

            if (Character.isDigit(currentChar) || currentChar == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < expression.length()
                        && (Character.isDigit(expression.charAt(i))
                                || expression.charAt(i) == '.')) {
                    sb.append(expression.charAt(i));
                    i++;
                }
                i--;
                values.push(Double.parseDouble(sb.toString()));
            } else if (currentChar == '(') {
                ops.push(currentChar);
            } else if (currentChar == ')') {
                while (!ops.isEmpty() && ops.peek() != '(') {
                    values.push(applyOperation(ops.pop(), values.pop(), values.pop()));
                }
                if (ops.isEmpty()) throw new RuntimeException("Mismatched parentheses.");
                ops.pop(); // Pop the opening parenthesis
            } else if (isOperator(currentChar)) {
                // while ops stack is not empty AND the top op has higher or equal precedence to the
                // current
                // op
                while (!ops.isEmpty() && hasPrecedence(ops.peek(), currentChar)) {
                    values.push(applyOperation(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(currentChar);
            }
        }
        while (!ops.isEmpty()) {
            if (ops.peek() == '(') throw new RuntimeException("Mismatched parentheses.");
            values.push(applyOperation(ops.pop(), values.pop(), values.pop()));
        }
        return values.pop();
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    /**
     * Returns true if op1 has higher or equal precedence to op2. Exponent operator '^' is
     * right-associative.
     */
    private boolean hasPrecedence(char op1, char op2) {
        if (op1 == '(' || op1 == ')') {
            return false;
        }
        // Exponentiation is right-associative
        if (op1 == '^' && op2 == '^') {
            return false;
        }
        return getPrecedence(op1) >= getPrecedence(op2);
    }

    /** Assigns a precedence level to an operator. */
    private int getPrecedence(char op) {
        return switch (op) {
            case '+', '-' -> 1;
            case '*', '/' -> 2;
            case '^' -> 3;
            default -> -1;
        };
    }

    private double applyOperation(char operator, double b, double a) {
        return switch (operator) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> {
                if (b == 0) throw new ArithmeticException("Cannot divide by zero");
                yield a / b;
            }
            case '^' -> Math.pow(a, b);
            default -> throw new UnsupportedOperationException("Unknown operator: " + operator);
        };
    }

    @Override
    public String manual() {
        return """
                NAME
                    math - Evaluate a mathematical expression

                SYNOPSIS
                    math <expression>

                DESCRIPTION
                    Evaluates a mathematical expression and returns the result. Supports +, -, *, /, ^, parentheses, and implicit multiplication.

                EXAMPLES
                    math 3+5
                    math (2+3)*4
                    math (5+3)2
                    math 2^3
                """;
    }
}
